<template>
  <el-dialog
    :model-value="modelValue"
    title="收银台"
    width="440px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="amount-line">
      应付金额 <span class="amount">¥{{ Number(amount || 0).toFixed(2) }}</span>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="paying" @click="submit">确认支付</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pay } from '@/api'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderNo: { type: String, default: '' },
  amount: { type: [Number, String], default: 0 }
})
const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()
const paying = ref(false)

// 支付成功后同步刷新余额，避免顶部仍显示旧值
function refreshBalance() {
  userStore.fetchUserInfo().catch(() => {})
}

async function submit() {
  if (!props.orderNo) return
  paying.value = true
  try {
    await pay({
      orderNo: props.orderNo,
      payType: 1,
      amount: Number(props.amount)
    })
    ElMessage.success('支付成功')
    emit('update:modelValue', false)
    emit('success')
    refreshBalance()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.amount-line {
  font-size: 14px;
  color: var(--ink-2);
  margin-bottom: 18px;
}

.amount {
  color: var(--brand);
  font-size: 26px;
  font-weight: 700;
  margin-left: 8px;
}
</style>

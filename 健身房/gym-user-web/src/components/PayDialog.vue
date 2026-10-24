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

    <el-radio-group v-model="payType" class="pay-types">
      <el-radio-button :label="1">余额支付</el-radio-button>
      <el-radio-button :label="2">支付宝</el-radio-button>
      <el-radio-button :label="3">微信支付</el-radio-button>
    </el-radio-group>

    <div v-if="payLink" class="pay-link">
      <p class="muted">预支付链接已生成（Mock）：</p>
      <el-input :model-value="payLink" readonly />
      <el-button type="success" class="simulate" :loading="simulating" @click="simulatePay">
        模拟支付成功
      </el-button>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button v-if="!payLink" type="primary" :loading="paying" @click="submit">
        确认支付
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { pay, mockCallback } from '@/api'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderNo: { type: String, default: '' },
  amount: { type: [Number, String], default: 0 }
})
const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()

const payType = ref(1)
const paying = ref(false)
const simulating = ref(false)
const payLink = ref('')

// 支付成功后同步刷新余额，避免顶部仍显示旧值
function refreshBalance() {
  userStore.fetchUserInfo().catch(() => {})
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      payType.value = 1
      payLink.value = ''
    }
  }
)

async function submit() {
  if (!props.orderNo) return
  paying.value = true
  try {
    const res = await pay({
      orderNo: props.orderNo,
      payType: payType.value,
      amount: Number(props.amount)
    })
    if (payType.value === 1) {
      ElMessage.success('余额支付成功')
      emit('update:modelValue', false)
      emit('success')
      refreshBalance()
    } else {
      payLink.value = res?.payLink || ''
      if (!payLink.value) {
        ElMessage.warning('未获取到支付链接，请重试')
      }
    }
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    paying.value = false
  }
}

async function simulatePay() {
  simulating.value = true
  try {
    await mockCallback(payType.value, props.orderNo, 'MOCK_' + Date.now())
    ElMessage.success('模拟支付成功，订单已更新')
    emit('update:modelValue', false)
    emit('success')
    refreshBalance()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    simulating.value = false
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

.pay-types {
  width: 100%;
  display: flex;
}

.pay-types :deep(.el-radio-button) {
  flex: 1;
}

.pay-types :deep(.el-radio-button__inner) {
  width: 100%;
}

.pay-link {
  margin-top: 18px;
  padding: 14px;
  border-radius: 10px;
  background: var(--bg);
}

.pay-link p {
  margin: 0 0 8px;
  font-size: 12px;
}

.simulate {
  margin-top: 12px;
  width: 100%;
}
</style>

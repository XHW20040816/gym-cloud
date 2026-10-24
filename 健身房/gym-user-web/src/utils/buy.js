import { createOrder, getOrderDetail } from '@/api'

/**
 * 创建订单并返回订单号与实际应付金额（payAmount 以后端计算为准）
 * itemType: 1 商品 2 课程 3 餐饮
 */
export async function createGoodsOrder(goodsId, quantity = 1, itemType = 1, deliveryInfo = '到店自提') {
  const orderNo = await createOrder({
    deliveryInfo,
    remark: '',
    items: [{ goodsId, quantity, itemType }]
  })
  const detail = await getOrderDetail(orderNo)
  const amount = detail?.order?.payAmount ?? 0
  return { orderNo, amount }
}

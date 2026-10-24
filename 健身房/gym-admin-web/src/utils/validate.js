export function validatePhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

export function validateEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

export function validateUsername(username) {
  const reg = /^[a-zA-Z0-9_]{4,16}$/
  return reg.test(username)
}

export function validatePassword(password) {
  return password && password.length >= 6 && password.length <= 20
}

export function validateIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return reg.test(idCard)
}

export function validatePrice(price) {
  return !isNaN(price) && Number(price) >= 0
}

export function validateInteger(num) {
  return Number.isInteger(Number(num)) && Number(num) >= 0
}

export function formatMoney(num) {
  if (num === null || num === undefined || num === '') return '0.00'
  return Number(num).toFixed(2)
}

export function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n) => (n < 10 ? '0' + n : n)
  return (
    d.getFullYear() +
    '-' +
    pad(d.getMonth() + 1) +
    '-' +
    pad(d.getDate()) +
    ' ' +
    pad(d.getHours()) +
    ':' +
    pad(d.getMinutes()) +
    ':' +
    pad(d.getSeconds())
  )
}

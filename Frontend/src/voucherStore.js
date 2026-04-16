import { defineStore } from 'pinia'

export const useVoucherStore = defineStore('voucher', {
  state: () => ({
    appliedVoucher: null,
    voucherCode: '',
    discountAmount: 0
  }),
  actions: {
    setVoucher(voucher, code, amount) {
      this.appliedVoucher = voucher
      this.voucherCode = code
      this.discountAmount = amount
    },
    clearVoucher() {
      this.appliedVoucher = null
      this.voucherCode = ''
      this.discountAmount = 0
    }
  }
})

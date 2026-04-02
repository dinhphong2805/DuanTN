<template>
  <div class="faq-page">
    <header class="faq-header">
      <h1>Câu hỏi thường gặp</h1>
      <p>Giải đáp thắc mắc về sản phẩm, giao hàng và đổi trả</p>
    </header>

    <section class="faq-list">
      <details
        v-for="(item, i) in faqs"
        :key="i"
        class="faq-item"
      >
        <summary class="faq-question">{{ item.q }}</summary>
        <p class="faq-answer">{{ item.a }}</p>
      </details>
    </section>
  </div>
</template>

<script setup>
const faqs = [
  {
    q: 'Thời gian giao hàng bao lâu?',
    a: 'Đơn hàng nội thành TP.HCM, Hà Nội thường giao trong 1-3 ngày. Các tỉnh khác 3-7 ngày tùy khu vực.',
  },
  {
    q: 'Tôi có thể đổi trả  1 sản phẩm không?',
    a: 'Bạn có thể đổi trả trong vòng 7 ngày nếu sản phẩm chưa qua sử dụng, còn nguyên tem. Liên hệ hotline hoặc qua trang Liên hệ để được hỗ trợ.',
  },
  {
    q: 'Làm sao để kiểm tra  haha hàng?',
    a: 'Sau khi đặt hàng, bạn sẽ nhận mã đơn qua email. Vào trang Tài khoản > Lịch sử đơn hàng để theo dõi trạng thái.',
  },
  {
    q: 'Phương thức thanh toán nào được hỗ trợ?',
    a: 'Chúng tôi hỗ trợ thanh toán COD (tiền mặt khi nhận hàng) và chuyển khoản ngân hàng.',
  },
  {
    q: 'Làm thế nào để sử dụng mã giảm giá?',
    a: 'Nhập mã giảm giá vào ô "Mã giảm giá" ở trang giỏ hàng hoặc thanh toán, bấm Áp dụng. Mã có hiệu lực khi đơn đạt điều kiện tối thiểu.',
  },
]
</script>

<style scoped>
.faq-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px 72px;
}

.faq-header {
  margin-bottom: 32px;
}

.faq-header h1 {
  font-size: 28px;
  font-weight: 800;
}

.faq-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 8px 0 0;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.faq-question {
  padding: 16px 18px;
  font-weight: 700;
  cursor: pointer;
  list-style: none;
}

.faq-question::-webkit-details-marker {
  display: none;
}

.faq-question::after {
  content: '+';
  float: right;
}

details[open] .faq-question::after {
  content: '−';
}

.faq-answer {
  padding: 0 18px 16px;
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}
</style>

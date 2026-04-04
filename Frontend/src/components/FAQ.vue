<template>
  <div class="faq-page">
    <header class="faq-header">
      <p class="faq-eyebrow">Hỗ trợ</p>
      <h1 class="faq-title">Câu hỏi thường gặp</h1>
      <p class="faq-subtitle">
        Những thắc mắc phổ biến về đặt hàng, giao hàng, đổi trả và thanh toán tại Kesn Store.
      </p>
    </header>

    <section class="faq-layout">
      <div class="faq-intro">
        <h2>Trước khi liên hệ</h2>
        <p>
          Bạn có thể tìm thấy câu trả lời rất nhanh trong danh sách bên cạnh.
          Nếu vẫn chưa rõ, hãy vào trang <strong>Liên hệ</strong> hoặc nhắn cho
          chúng tôi qua hộp chat ở góc màn hình.
        </p>
        <ul class="faq-highlights">
          <li>Miễn phí giao từ 3.000.000₫, đổi trả trong 14 ngày</li>
          <li>Hỗ trợ tư vấn size theo từng dòng sản phẩm</li>
          <li>Thông báo trạng thái đơn hàng theo thời gian thực</li>
        </ul>
      </div>

      <section class="faq-list" aria-label="Danh sách câu hỏi thường gặp">
        <details
          v-for="(item, i) in faqs"
          :key="i"
          class="faq-item"
        >
          <summary class="faq-question">
            <span>{{ item.q }}</span>
          </summary>
          <p class="faq-answer">
            {{ item.a }}
          </p>
        </details>
      </section>
    </section>
  </div>
</template>

<script setup>
const faqs = [
  {
    q: 'Thời gian giao hàng bao lâu?',
    a: 'Đơn hàng nội thành TP.HCM, Hà Nội thường giao trong 1–3 ngày làm việc. Các tỉnh thành khác từ 3–7 ngày tùy khu vực và đơn vị vận chuyển.',
  },
  {
    q: 'Tôi có thể đổi hoặc trả sản phẩm không?',
    a: 'Bạn có thể đổi hoặc trả trong vòng 14 ngày kể từ khi nhận hàng nếu sản phẩm chưa qua sử dụng, còn đầy đủ tem mác và hộp. Vui lòng giữ hóa đơn/ thông tin đơn hàng để được hỗ trợ nhanh hơn.',
  },
  {
    q: 'Làm sao để kiểm tra trạng thái đơn hàng?',
    a: 'Sau khi đặt hàng, bạn sẽ nhận được mã đơn qua email. Vào trang Tài khoản → Lịch sử đơn hàng để theo dõi trạng thái, hoặc liên hệ hotline kèm mã đơn để được kiểm tra.',
  },
  {
    q: 'Phương thức thanh toán nào được hỗ trợ?',
    a: 'Kesn Store hiện hỗ trợ thanh toán COD (tiền mặt khi nhận hàng) và chuyển khoản ngân hàng. Một số chương trình có thể hỗ trợ thêm ví điện tử, thông tin sẽ hiển thị ở bước thanh toán.',
  },
  {
    q: 'Làm thế nào để sử dụng mã giảm giá?',
    a: 'Nhập mã giảm giá vào ô “Mã giảm giá” ở trang giỏ hàng hoặc thanh toán, sau đó bấm Áp dụng. Mã sẽ có hiệu lực khi đơn hàng đáp ứng đúng điều kiện về giá trị và thời gian sử dụng.',
  },
]
</script>

<style scoped>
.faq-page {
  max-width: 1040px;
  margin: 0 auto;
  padding: 32px 16px 72px;
}

.faq-header {
  margin-bottom: 32px;
}

.faq-eyebrow {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9ca3af;
}

.faq-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.faq-subtitle {
  margin: 10px 0 0;
  max-width: 540px;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.faq-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.3fr);
  gap: 32px;
  align-items: flex-start;
}

.faq-intro {
  padding: 22px 20px;
  border-radius: 18px;
  border: 1px solid #e5e7eb;
  background: linear-gradient(145deg, #f9fafb, #eef2ff);
}

.faq-intro h2 {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 700;
}

.faq-intro p {
  margin: 0 0 14px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.faq-highlights {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #374151;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.04);
}

.faq-question {
  padding: 16px 20px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  list-style: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.faq-question span {
  flex: 1;
}

.faq-question::-webkit-details-marker {
  display: none;
}

.faq-question::after {
  content: '+';
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
  color: #6b7280;
}

details[open] .faq-question::after {
  content: '−';
}

.faq-answer {
  padding: 0 20px 16px;
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .faq-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

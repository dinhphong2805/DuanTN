/**
 * Bảng quy đổi size giày — tham khảo chuẩn phổ biến (tương đương nhiều thương hiệu quốc tế).
 * Lưu ý: 39W là size nữ 39; size 39 (nam) khác chiều dài chân.
 */
export const shoeSizeGuideSections = [
  {
    label: 'Trẻ em',
    rows: [
      { size: '28', cm: '18.5' },
      { size: '29', cm: '19' },
      { size: '30', cm: '19.5' },
      { size: '31', cm: '20' },
      { size: '32', cm: '20.5' },
      { size: '33', cm: '21' },
      { size: '34', cm: '21.5' },
      { size: '35', cm: '22' },
    ],
  },
  {
    label: 'Nữ',
    rows: [
      { size: '36', cm: '22.7' },
      { size: '37', cm: '23.3' },
      { size: '38', cm: '24' },
      { size: '39W', cm: '24.5', note: 'Size nữ 39' },
    ],
  },
  {
    label: 'Nam',
    rows: [
      { size: '39', cm: '25' },
      { size: '40', cm: '25.7' },
      { size: '41', cm: '26.3' },
      { size: '42', cm: '27' },
      { size: '43', cm: '27.5' },
      { size: '44', cm: '28' },
      { size: '45', cm: '28.7' },
      { size: '46', cm: '29.3' },
    ],
  },
]

export const shoeSizeGuideCopy = {
  title: 'Cách đo chân bằng thước',
  subtitle: 'Hướng dẫn đo bàn chân',
  measureText:
    'Đo chiều dài chân từ đầu ngón chân dài nhất (ngón cái hoặc ngón trỏ) đến cuối gót chân, rồi đối chiếu với bảng size bên cạnh.',
  notes: [
    'Size giày trên cửa hàng tương đương size chuẩn của các thương hiệu quốc tế phổ biến.',
    '39W là size nữ 39, gần tương đương nam ~38.5.',
    'Size 39 (nam) tương đương nam 39 hoặc nữ ~40.',
  ],
}

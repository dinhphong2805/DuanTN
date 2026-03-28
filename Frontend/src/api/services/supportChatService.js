import client from '../client'

/**
 * @param {{ role: 'user' | 'model', text: string }[]} messages
 * @returns {Promise<{ reply: string }>}
 */
export async function sendSupportChat(messages) {
  const { data } = await client.post('chat/support', { messages })
  return data
}

export async function getSupportChatStatus() {
  const { data } = await client.get('chat/support/status')
  return data
}

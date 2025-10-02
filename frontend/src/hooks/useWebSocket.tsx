import React, { useContext } from 'react'
import { WebSocketContext } from '../providers/WebSocketProvider'

const useWebSocket = () => {
  const context = useContext(WebSocketContext)
	if (!context) {
    throw new Error("Invalid use of use WebSocket");
  }
  return context;
}

export default useWebSocket;

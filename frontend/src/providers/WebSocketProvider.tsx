import React, { createContext, useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import useAuth from '../hooks/useAuth'

interface WebSocketContextType {
  client: Client | null;
  connected: boolean;
}

export const WebSocketContext = createContext<WebSocketContextType>({
  client: null,
  connected: false,
})

const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { accessToken } = useAuth();
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!accessToken) return;
    const client = new Client({
      brokerURL: `ws://localhost:8222/ws?token=${accessToken}`,
      reconnectDelay: 5000,
      debug: (str) => console.log("[STOMP]", str),
      onConnect: () => {
        console.log("Connected to WebSocket");
        setConnected(true);
      },
      onDisconnect: () => {
        console.log("Disconnected from WebSocket");
        setConnected(false);
      },
    })

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [accessToken])
  
  return (
     <WebSocketContext.Provider value={{ client: clientRef.current, connected }}>
      {children}
    </WebSocketContext.Provider>
  )
}

export default WebSocketProvider;

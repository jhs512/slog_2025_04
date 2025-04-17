"use client";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

// SockJS 연결 옵션을 개선 - 전송 방식 순서 변경 및 타임아웃 증가
const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_BASE_URL}/ws`);

let stompClientConnected = false;
// 구독 대기 큐 추가
const subscriptionQueue: {
  destination: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  callback: (message: any) => void;
}[] = [];
// 활성 구독 저장소 추가
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const activeSubscriptions: { [key: string]: any } = {};

// 발행 대기 큐 추가
const publishQueue: {
  destination: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  body: any;
}[] = [];

const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {},
  onConnect: () => {
    stompClientConnected = true;

    // 연결 성공 시 대기 중인 구독 처리
    while (subscriptionQueue.length > 0) {
      const { destination, callback } = subscriptionQueue.shift()!;
      performSubscribe(destination, callback);
    }

    // 연결 성공 시 대기 중인 발행 처리
    while (publishQueue.length > 0) {
      const { destination, body } = publishQueue.shift()!;
      performPublish(destination, body);
    }
  },
});

// 실제 구독을 수행하는 내부 함수
const performSubscribe = (
  destination: string,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  callback: (message: any) => void,
) => {
  const subscription = stompClient.subscribe(
    destination,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (message: any) => {
      callback(message);
    },
  );

  activeSubscriptions[destination] = subscription;
};

// 구독 함수
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const subscribe = (destination: string, callback: (message: any) => void) => {
  if (!stompClientConnected) {
    subscriptionQueue.push({ destination, callback });
  } else {
    performSubscribe(destination, callback);
  }
};

// 구독 해제 함수
const unsubscribe = (destination: string) => {
  if (activeSubscriptions[destination]) {
    activeSubscriptions[destination].unsubscribe();
    delete activeSubscriptions[destination];
  }
};

// 실제 발행을 수행하는 내부 함수
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const performPublish = (destination: string, body: any) => {
  stompClient.publish({
    destination,
    body: JSON.stringify(body),
  });
};

// 발행 함수
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const publish = (destination: string, body: any) => {
  if (!stompClientConnected) {
    publishQueue.push({ destination, body });
  } else {
    performPublish(destination, body);
  }
};

// 웹소켓 연결 상태 확인 함수
const isConnected = () => {
  return stompClientConnected && !!stompClient.connected;
};

stompClient.activate();

export default stompClient;
export { subscribe, unsubscribe, publish, isConnected };

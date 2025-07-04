import { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import * as S from "./Chat.style";
import { Link, useParams } from "react-router-dom"; 
import { API_URLS } from "../../consts";

const formatSummary = (text) => {
  if (!text) return [];

  // 문단 나누기
  const topicBlocks = text.split(/(?=주제\s*\d*:)/g);
  const formattedParagraphs = [];

  topicBlocks.forEach((block) => {
    const sentences = block.trim().split(/(?<=[.!?])\s+/);
    const paragraphSize = 1;

    for (let i = 0; i < sentences.length; i += paragraphSize) {
      const para = sentences.slice(i, i + paragraphSize).join(" ");
      formattedParagraphs.push(para);
    }
  });

  return formattedParagraphs;
};

export function Chat() {
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState(""); 
  const [userId, setUserId] = useState(() => Number(localStorage.getItem("userId") || 0)); 
  const [username, setUserName] = useState(() => localStorage.getItem("userName") || "사용자"); 
  const [stompClient, setStompClient] = useState(null);
  const [memoContent, setMemoContent] = useState("");
  const [isMemoVisible, setIsMemoVisible] = useState(false);
  const [isFreeDiscussion, setIsFreeDiscussion] = useState(false);
  const { clubId } = useParams();
  const [data, setData] = useState(null);
  
  const token = localStorage.getItem("token") || "";
  
  useEffect(() => {
    console.log("[SOCKJS] 연결 시도: https://modam.duckdns.org/chat");
  
    const socket = new SockJS('https://modam.duckdns.org/chat');

    // console: 소켓 연결 후 이벤트 확인
    socket.onopen = () => {
      console.log("[SOCKJS] 소켓 연결 성공");
    };
    socket.onclose = (e) => {
      console.error("[SOCKJS] 소켓 연결 종료됨:", e);
    };
    socket.onerror = (e) => {
      console.error("[SOCKJS] 소켓 오류 발생:", e);
    };
    
    const client = new Client({
      webSocketFactory: () => socket,
       connectHeaders: {
        Authorization: `Bearer ${token}`  
      },
      reconnectDelay: 5000, 
      onConnect: () => {
        console.log("WebSocket 연결 성공"); 
        window.stompClient = client; 

        client.publish({
          destination: `/app/chat/${clubId}`,
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            messageType: "ENTER",
            clubId: parseInt(clubId),
            userId,
            userName: username,
            content: `${username}님이 입장하셨습니다.`,
          }),
        });

        client.subscribe(`/topic/chat/${clubId}`, async (message) => {
          const receivedMessage = JSON.parse(message.body);

          console.log("[DEBUG] 받은 메시지:", receivedMessage);
          
          // 자유토론 시작 메시지 감지
          if (receivedMessage.messageType === "FREE_DISCUSSION_NOTICE") {
            setIsFreeDiscussion(true);
          }
          // 자유토론 종료 (주제 전환 또는 종료)
          if (
              receivedMessage.messageType === "MAINTOPIC" ||
              receivedMessage.messageType === "END_NOTICE"
          ) {
            setIsFreeDiscussion(false);
          }

          //모임 종료 시 메모 확정
          if (receivedMessage.messageType === "END_NOTICE") {
            await finalizeMemo(); 
          }

          setMessages((prevMessages) => [...prevMessages, receivedMessage]);  
        });
        
      },
      onStompError: (error) => {
        console.error("STOMP 오류:", error);
      },
    });

      const fetchClubDetail = async () => {
      try {
        const response = await fetch(API_URLS.bookclubDetail(clubId, userId), {
          method: "GET",
         });

        if (!response.ok) throw new Error("모임 정보 로드 실패");

        const result = await response.json();
        setData(result); // 모임 정보 저장

      } catch (error) {
        console.error("모임 상세 정보 불러오기 실패:", error);
      }
    };

    fetchClubDetail();
    client.activate(); // 연결 시작
    setStompClient(client);
    loadMemo(); 

    return () => {
      client.deactivate(); // 컴포넌트 언마운트 시 연결 해제
    };
  }, []);

  const sendMessage = () => {
    if (!stompClient || !stompClient.connected) {
      console.error("STOMP 클라이언트가 연결되지 않았습니다.");
      return;
    }

    if (message.trim()) {
      const parsedClubId = parseInt(clubId);
      console.log("보낼 clubId:", parsedClubId);

      const chatMessage = {
        messageType: isFreeDiscussion ? "FREE_DISCUSSION" : "DISCUSSION", // soo:demo02-2: 여기 조건 추가!
        clubId: parseInt(clubId), 
        userId, 
        userName: username, 
        content: message,
      };

      // WebSocket 연결이 되어 있을 때는 서버로 보내기
      if (stompClient && stompClient.connected) {
        stompClient.publish({
          destination: `/app/chat/${clubId}`,
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(chatMessage),
        });
      } else {
        // WebSocket 연결 안 됐을 때는 화면에만 추가
        console.warn("WebSocket 미연결 상태, 로컬에 메시지 추가됨");
        setMessages((prevMessages) => [...prevMessages, chatMessage]);
      }

      setMessage("");
    }
  };

  const handleMemoChange = (e) => {
    setMemoContent(e.target.value);
  };

  const toggleMemo = () => {
    setIsMemoVisible(!isMemoVisible);
  };

  //메모 저장
  const saveMemo = async () => {
    try {
     const response = await fetch(API_URLS.memo(clubId, userId), {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          content: memoContent,
        }),
      });

      if (!response.ok) throw new Error("서버 응답 실패");

      alert("메모가 저장되었습니다!");
    } catch (error) {
      console.error("메모 저장 실패:", error);
      alert("메모 저장 중 오류가 발생했습니다.");
    }
  };

  //메모 조회
  const loadMemo = async () => {
    try {
      const response = await fetch(API_URLS.memo(clubId, userId), {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("메모 조회 실패");

      const data = await response.json();
      setMemoContent(data.content || "");
    } catch (error) {
      console.error("메모 조회 중 오류:", error);
    }
  };


  //메모 확정
  const finalizeMemo = async () => {
    try {
      const response = await fetch(API_URLS.finalizeMemo(clubId, userId), {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("메모 확정 실패");
      console.log(" 메모 확정 완료");
    } catch (error) {
      console.error("메모 확정 오류:", error);
    }
  };

  return (
      <S.Container>
        <S.ChatSection>
          <S.Header>
            <S.Title>{data?.bookTitle || "독서모임"}</S.Title>
            <S.RightSection>
              <S.NoteText onClick={toggleMemo}>
                {isMemoVisible ? "메모 닫기" : "메모 열기"}
              </S.NoteText>
              <Link to="/Main">
                <S.ExitButton>나가기</S.ExitButton>
              </Link>
            </S.RightSection>
          </S.Header>

          <S.ChatBox>
          {messages.map((msg, index) => {
            const isMine = msg.userName === username;
            const isAI = msg.userId === 0;

            const userName = isAI ? "AI 진행자" : msg.userName;
            const avatar = "/assets/chatbot.png";
            const messageStyle = isAI ? "bot-message" : isMine ? "my-message" : "user-message";

            return (
              <S.Message key={index} className={messageStyle}>
                {isAI && <S.Avatar src={avatar} alt="AI avatar" />}
                  <div>
                    <strong>{userName}</strong>
                    {msg.userId === 0 && msg.messageType === "SUMMARY" ? (
                      formatSummary(msg.content).map((para, i) => <p key={i}>{para}</p>)
                    ) : (
                      <div>{msg.content}</div>
                    )}
                </div>
              </S.Message>
            );
          })}

          </S.ChatBox>
            <S.InputContainer>
              <S.Input
                type="text"
                placeholder="여기에 내용을 입력해 주세요."
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyPress={(e) => e.key === "Enter" && sendMessage()}
              />
              <S.SendButton onClick={sendMessage}>보내기</S.SendButton>
            </S.InputContainer>
        </S.ChatSection>

        {isMemoVisible && (
            <S.MemoSection>
              <S.MemoTitle>메모</S.MemoTitle>
              <S.MemoInput
                  value={memoContent}
                  onChange={handleMemoChange}
                  placeholder="여기에 메모를 입력하세요."
              />
              <S.SaveMemoButton onClick={saveMemo}>저장</S.SaveMemoButton>
            </S.MemoSection>
        )}
      </S.Container>
  );
}
let stompClient = null;
let currentRoomId = null; 
let currentUserId = null; 
let currentUserRole = null; // 방 목록을 가져오려면 ROLE이 필요할 수 있습니다.
let myChatRooms = [];       // 내가 속한 방 목록 리스트 전역 캐시

// 1. 메인 버튼: 창 열기/닫기
function toggleChatWindow() {
    const chatWindow = document.getElementById('bugbug-chat-window');

    // 위젯에 숨겨진 세션 정보에서 진짜 내 유저 번호와 역할을 동적으로 가져옵니다.
    const userInput = document.getElementById('widget-user-id');
    const roleInput = document.getElementById('widget-user-role');

    currentUserId = userInput && userInput.value ? parseInt(userInput.value) : 1;
    currentUserRole = roleInput && roleInput.value ? roleInput.value : "USER";

    if (chatWindow.style.display === 'block') {
        chatWindow.style.display = 'none';
        disconnectChat();
    } else {
        chatWindow.style.display = 'block';
        // 창이 열릴 때 항상 '방 목록' 화면부터 보여줍니다.
        showRoomListView();
    }
}

// 2. 채팅방 목록 화면 보여주기
async function showRoomListView() {
    document.getElementById('bugbug-chat-room-list-view').style.display = 'flex';
    document.getElementById('bugbug-chat-room-detail-view').style.display = 'none';
    
    const listContainer = document.getElementById('bugbug-room-list');
    listContainer.innerHTML = '<div style="padding:20px; text-align:center;">로딩 중...</div>';

    try {
        // 서버에서 내 채팅방 목록 가져오기
        const response = await fetch(`/api/chats?userId=${currentUserId}&role=${currentUserRole}`, {
            credentials: "include"
        });
        
        if (!response.ok) {
            listContainer.innerHTML = '<div style="padding:20px; text-align:center; color:red;">목록을 불러오지 못했습니다.</div>';
            return;
        }

        const rooms = await response.json();
        myChatRooms = rooms; // 전역 캐시에 저장
        listContainer.innerHTML = '';
        
        if (rooms.length === 0) {
            listContainer.innerHTML = '<div style="padding:20px; text-align:center; color:#888;">참여 중인 채팅방이 없습니다.</div>';
            return;
        }

        // 가져온 방들을 화면에 리스트로 그립니다.
        rooms.forEach(room => {
            const item = document.createElement('div');
            item.className = 'room-item';
            // 방을 클릭하면 해당 방으로 입장(대화창 열기)합니다.
            item.onclick = () => enterRoom(room.roomId, room.otherNickname);
            
            item.innerHTML = `
                <div class="room-avatar">👤</div>
                <div class="room-info">
                    <div class="room-title">${room.otherNickname} 님과의 대화</div>
                    <div class="room-other">의뢰: ${room.title}</div>
                </div>
            `;
            listContainer.appendChild(item);
        });

    } catch (error) {
        console.error("방 목록 조회 에러", error);
    }
}

// 3. 채팅방 입장 (대화창으로 화면 전환)
function enterRoom(roomId, otherNickname) {
    currentRoomId = roomId;
    
    // 화면 전환
    document.getElementById('bugbug-chat-room-list-view').style.display = 'none';
    document.getElementById('bugbug-chat-room-detail-view').style.display = 'flex';
    
    // 상단 제목 변경
    document.getElementById('bugbug-chat-title').innerText = otherNickname;
    
    // 예약 현황 표시 및 버튼 노출 여부 처리
    const roomInfo = myChatRooms.find(r => r.roomId === roomId);
    const statusBar = document.getElementById('bugbug-chat-status-bar');
    const statusText = document.getElementById('bugbug-reservation-status');
    const reserveBtn = document.getElementById('bugbug-reserve-btn');
    
    if (statusBar && statusText && reserveBtn) {
        statusBar.style.display = 'flex';
        if (roomInfo && roomInfo.reservedAt) {
            statusText.innerText = '📌 매칭 상태: 예약 완료';
            reserveBtn.style.display = 'none';
        } else {
            statusText.innerText = '📌 매칭 상태: 대기 중';
            // 의뢰인(USER)일 때만 예약 완료하기 버튼 표시
            if (currentUserRole === 'USER') {
                reserveBtn.style.display = 'block';
            } else {
                reserveBtn.style.display = 'none';
            }
        }
    }
    
    // 채팅 연결 및 이전 메시지 불러오기
    connectChat();
}

// 4. 뒤로 가기 (목록으로 돌아가기)
function goBackToList() {
    disconnectChat(); // 현재 방 통신 끊기
    currentRoomId = null;
    showRoomListView(); // 다시 목록 화면으로
}


// --- 아래부터는 기존과 동일한 소켓 & 메시지 로직 ---

function connectChat() {
    const socket = new SockJS('/ws/chats');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        loadPreviousMessages();

        stompClient.subscribe('/topic/chat/room/' + currentRoomId, function (message) {
            showMessage(JSON.parse(message.body));
        });
    });
}

function disconnectChat() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
}

function sendChatMessage() {
    const input = document.getElementById('bugbug-chat-input');
    const content = input.value.trim();
    
    if (content && stompClient) {
        const chatMessage = {
            roomId: currentRoomId,
            senderId: currentUserId,
            content: content,
            messageType: "TEXT"
        };
        stompClient.send("/app/chat/message", {}, JSON.stringify(chatMessage));
        input.value = '';
    }
}

async function loadPreviousMessages() {
    const chatBox = document.getElementById('bugbug-chat-messages');
    chatBox.innerHTML = ''; 
    
    try {
        const response = await fetch(`/api/chats/${currentRoomId}/messages`, {
            credentials: "include" 
        });
        
        if (response.ok) {
            const messages = await response.json();
            messages.reverse().forEach(msg => showMessage(msg));
        }
    } catch (error) {
        console.error('이전 메시지 에러:', error);
    }
}

function showMessage(msg) {
    const chatBox = document.getElementById('bugbug-chat-messages');
    const isMine = (msg.senderId == currentUserId);
    
    const bubbleWrapper = document.createElement('div');
    bubbleWrapper.className = 'chat-bubble ' + (isMine ? 'chat-mine' : 'chat-other');
    
    let mediaHtml = '';
    if (msg.messageType === 'IMAGE') {
        mediaHtml = `<img src="${msg.fileUrl}" class="chat-media">`;
    } else if (msg.messageType === 'VIDEO') {
        mediaHtml = `<video src="${msg.fileUrl}" controls class="chat-media"></video>`;
    } else if (msg.messageType === 'AUDIO') {
        mediaHtml = `<audio src="${msg.fileUrl}" controls class="chat-media"></audio>`;
    }

    bubbleWrapper.innerHTML = `
        ${isMine ? '' : `<div style="font-size:11px; margin-bottom:3px; color:#555;">${msg.senderNickname}</div>`}
        <div>${msg.content}</div>
        ${mediaHtml}
    `;
    
    chatBox.appendChild(bubbleWrapper);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function handleEnterKey(event) {
    if (event.key === 'Enter') {
        sendChatMessage();
    }
}

function triggerFileInput() {
    document.getElementById('bugbug-file-input').click();
}

async function uploadChatFile() {
    const fileInput = document.getElementById('bugbug-file-input');
    if (fileInput.files.length === 0) return;

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("file", file);
    formData.append("senderId", currentUserId);

    let messageType = 'IMAGE';
    if (file.type.startsWith('video/')) messageType = 'VIDEO';
    if (file.type.startsWith('audio/')) messageType = 'AUDIO';
    
    formData.append("messageType", messageType);

    try {
        const response = await fetch(`/api/chats/${currentRoomId}/files`, {
            method: "POST",
            body: formData,
            credentials: "include" 
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            alert('파일 전송 실패: ' + errorText);
        }
    } catch (error) {
        console.error('파일 업로드 오류:', error);
    } finally {
        fileInput.value = ''; 
    }
}

// --- 예약 기능 관련 핸들러 함수 추가 ---

async function confirmReservation() {
    if (!confirm('이 의뢰를 예약 완료 상태로 변경하시겠습니까?')) {
        return;
    }
    
    // 현재 시각 기준의 ISO 문자열 (Spring Boot LocalDateTime 파싱용: YYYY-MM-DDTHH:mm:ss)
    const localIsoString = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19);
    
    try {
        const response = await fetch(`/api/chats/${currentRoomId}/reservation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reservedAt: localIsoString }),
            credentials: 'include'
        });
        
        if (response.ok) {
            alert('예약이 성공적으로 완료되었습니다.');
            
            // 현황 바 UI 로컬 업데이트
            const statusText = document.getElementById('bugbug-reservation-status');
            if (statusText) {
                statusText.innerText = '📌 매칭 상태: 예약 완료';
            }
            const reserveBtn = document.getElementById('bugbug-reserve-btn');
            if (reserveBtn) {
                reserveBtn.style.display = 'none';
            }
            
            // 전역 캐시 업데이트
            const roomInfo = myChatRooms.find(r => r.roomId === currentRoomId);
            if (roomInfo) {
                roomInfo.reservedAt = localIsoString;
            }
            
            // 상세 정보 페이지의 상태 배지 동적 업데이트
            const badge = document.querySelector('.status-badge');
            if (badge) {
                badge.innerText = '예약 완료';
                badge.style.backgroundColor = '#4e73df';
                badge.style.color = '#FFFFFF';
            }
            
            // 확실한 페이지 반영을 위해 1초 후 새로고침
            setTimeout(() => {
                location.reload();
            }, 1000);
            
        } else {
            const errorText = await response.text();
            alert('예약 처리에 실패했습니다: ' + errorText);
        }
    } catch (error) {
        console.error('예약 에러:', error);
        alert('예약 진행 중 오류가 발생했습니다.');
    }
}

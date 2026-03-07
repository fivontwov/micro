```
[Alice Tab]                [Forum Service]              [Bob Tab]
│                           │                           │
│─ SEND /app/forum/1/typing ─→                          │
│  { userId:1, action:TYPING}                           │
│                           │── @MessageMapping ──┐     │
│                           │   bổ sung topicId   │     │
│                           │   + timestamp       │     │
│                           └─ broadcast ─────────→     │
│                    /topic/forum/1/typing              │
│                                              "Alice đang gõ..." ✅
```
Mở http://localhost:{port}/ws-test.html trên 2 tab

Tab 1: User Alice, UserID=1, TopicID=1 → Connect
Tab 2: User Bob, UserID=2, TopicID=1 → Connect

Bước 3: Alice gõ vào textarea → Tab của Bob hiện "Alice đang gõ..." với animation


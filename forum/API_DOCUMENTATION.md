# Forum API Documentation

## Base URL
```
http://localhost:8080
```

## Topics Endpoints

### 1. Create Topic
**POST** `/topics`

Create a new forum topic.

**Request Body:**
```json
{
  "userId": 1,
  "title": "What is Spring Boot?",
  "body": "I want to learn Spring Boot. Can someone explain the basics?"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "title": "What is Spring Boot?",
  "body": "I want to learn Spring Boot. Can someone explain the basics?",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

---

### 2. Get All Topics
**GET** `/topics`

Retrieve all forum topics.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "What is Spring Boot?",
    "body": "I want to learn Spring Boot...",
    "createdAt": "2026-01-15T10:30:00Z"
  }
]
```

---

### 3. Get Topic by ID
**GET** `/topics/{topicId}`

Retrieve a specific topic by ID.

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "title": "What is Spring Boot?",
  "body": "I want to learn Spring Boot...",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

---

### 4. Delete Topic
**DELETE** `/topics/{topicId}`

Delete a topic by ID.

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Response:** `204 No Content`

---

## Comments Endpoints

### 1. Add Comment
**POST** `/topics/{topicId}/comments`

Add a comment to a topic.

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Request Body:**
```json
{
  "userId": 2,
  "parentCommentId": null,
  "body": "Spring Boot is great! It makes development easier."
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "topicId": 1,
  "userId": 2,
  "parentCommentId": null,
  "body": "Spring Boot is great!",
  "createdAt": "2026-01-15T10:35:00Z"
}
```

---

### 2. List Comments for Topic
**GET** `/topics/{topicId}/comments`

Get all comments for a specific topic.

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "topicId": 1,
    "userId": 2,
    "parentCommentId": null,
    "body": "Spring Boot is great!",
    "createdAt": "2026-01-15T10:35:00Z"
  }
]
```

---

### 3. Get Comment by ID
**GET** `/topics/{topicId}/comments/{commentId}`

Get a specific comment.

**Path Parameters:**
- `topicId` (Long) - The topic ID
- `commentId` (Long) - The comment ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "topicId": 1,
  "userId": 2,
  "parentCommentId": null,
  "body": "Spring Boot is great!",
  "createdAt": "2026-01-15T10:35:00Z"
}
```

---

### 4. Delete Comment
**DELETE** `/topics/{topicId}/comments/{commentId}`

Delete a comment.

**Path Parameters:**
- `topicId` (Long) - The topic ID
- `commentId` (Long) - The comment ID

**Response:** `204 No Content`

---

## Votes Endpoints

### 1. Vote Up
**POST** `/topics/{topicId}/votes`

Vote up a topic (+1).

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Request Body:**
```json
{
  "userId": 2,
  "value": 1
}
```

**Response:** `200 OK`

---

### 2. Vote Down
**POST** `/topics/{topicId}/votes`

Vote down a topic (-1).

**Path Parameters:**
- `topicId` (Long) - The topic ID

**Request Body:**
```json
{
  "userId": 3,
  "value": -1
}
```

**Response:** `200 OK`

---

## Data Models

### Topic
```json
{
  "id": 1,
  "userId": 1,
  "title": "string",
  "body": "string",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

### Comment
```json
{
  "id": 1,
  "topicId": 1,
  "userId": 2,
  "parentCommentId": null,
  "body": "string",
  "createdAt": "2026-01-15T10:35:00Z"
}
```

### Vote
```json
{
  "userId": 1,
  "value": 1  // 1 for upvote, -1 for downvote
}
```

---

## Testing with Postman

1. Import `POSTMAN_COLLECTION.json` into Postman
2. Update the endpoint URLs if your server is running on a different port
3. Start testing the APIs!

## Example Flow

1. **Create Topic** - POST `/topics`
2. **Get All Topics** - GET `/topics`
3. **Add Comment** - POST `/topics/1/comments`
4. **List Comments** - GET `/topics/1/comments`
5. **Vote** - POST `/topics/1/votes`

CREATE TABLE IF NOT EXISTS topics (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255),
  body TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGSERIAL PRIMARY KEY,
  topic_id BIGINT REFERENCES topics(id) ON DELETE CASCADE,
  parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL,
  body TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS topic_votes (
  id BIGSERIAL PRIMARY KEY,
  topic_id BIGINT REFERENCES topics(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL,
  value INT CHECK (value IN (-1,1)),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, topic_id)
);

CREATE INDEX IF NOT EXISTS idx_topics_user_id ON topics(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_topic_id ON comments(topic_id);
CREATE INDEX IF NOT EXISTS idx_topic_votes_topic_id ON topic_votes(topic_id);

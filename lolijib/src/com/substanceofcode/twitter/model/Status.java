/*
 * StatusEntry.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.substanceofcode.twitter.model;

import java.util.Date;
import com.substanceofcode.utils.StringUtil;

/**
 * StatusEntry
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Status {

	private long id;
    private String screenName;
    private String statusText;
    private Date date;
	private String source;
	private long replyToStatus;
	private String replyToScreenName;
	private boolean favorited;
	private User user;

    /** Creates a new instance of StatusEntry
     *  
     * @param id
     * @param screenName
     * @param statusText
     * @param date
     * @param source
     * @param favorited
     */
    public Status(long id, String screenName, String statusText, Date date, String source, boolean favorited) {
		this.id = id;
        this.screenName = screenName;
        this.statusText = statusText;
        this.date = date;
        this.source = source;
        this.favorited = favorited;
    }

	public long getId() {
		return id;
	}
    
    public String getText() {
        return statusText;
    }
    
    public String getScreenName() {
        return screenName;
    }
    
    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

	public boolean getFavorited() {
		return favorited;
	}

	public String getStatusText() {
		return statusText;
	}

	public long getReplyToStatus() {
		return replyToStatus;
	}

	public void setReplyToStatus(long replyToStatus) {
		this.replyToStatus = replyToStatus;
	}

	public String getReplyToScreenName() {
		return replyToScreenName;
	}

	public void setReplyToScreenName(String replyToScreenName) {
		this.replyToScreenName = replyToScreenName;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRetweet(int maxLen) {
		int len = (maxLen-6)-screenName.length(); // 6 is from "rt @" + ": "

		String text = statusText;
		if (text.length() > len) {
			String[] chunks = StringUtil.split(text, " ");
			int chunksLen = chunks.length;
			int urlLen    = 0;
			int nonUrlLen = 0;

			// find urls and calculate space needed for urls
			for (int i=0; i < chunksLen; i++) {
				if (chunks[i].startsWith("http://") ||
					chunks[i].startsWith("https://") ||
					chunks[i].startsWith("www.")) {
					urlLen = urlLen + chunks[i].length();
				}
			}
			nonUrlLen = len-urlLen-1; // available space for non-url chars

			StringBuffer buf = new StringBuffer(153);
			int i = 0;
			while ((buf.length() < len) && (i < chunksLen)) {
				String chunk = chunks[i];
				if (chunks[i].startsWith("http://") ||
					chunks[i].startsWith("https://") ||
					chunks[i].startsWith("www.")) {
					if (buf.length() > 0) {
						buf.append(' ');
						nonUrlLen = nonUrlLen - 1;
					}
					buf.append(chunk);
					urlLen = urlLen - chunk.length();
				} else {
					if (nonUrlLen > 3) {
						if (buf.length() > 0) {
							buf.append(' ');
							nonUrlLen = nonUrlLen - 1;
						}
						if (nonUrlLen <= chunk.length()) {
							chunk = chunk.substring(0, nonUrlLen-3)+"..";
						}
						buf.append(chunk);
						nonUrlLen = nonUrlLen - chunk.length();
					}
				}
				i++;
			}
			
			// if it still exceed the possible max, signal it by ".."
			if (buf.length() > len) {
				text = buf.toString().substring(0, len-2) + "..";
			} else {
				text = buf.toString();
			}
		}
		return "RT @"+screenName+": "+text;
	}

}

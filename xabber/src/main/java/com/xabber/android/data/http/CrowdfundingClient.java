package com.xabber.android.data.http;

import com.xabber.android.R;
import com.xabber.android.data.Application;
import com.xabber.android.data.database.realm.CrowdfundingMessage;
import com.xabber.android.data.xaccount.HttpApiManager;

import java.util.List;

import rx.Single;
import rx.functions.Func1;

public class CrowdfundingClient {

    public static Single<List<CrowdfundingMessage>> getLeaderAndFeed() {
        if (getAPIKey().length() < 20) return Single.error(new Throwable("API key not provided"));
        return HttpApiManager.getCrowdfundingApi().getLeader(getAPIKey())
            .flatMap(new Func1<Message, Single<? extends CrowdfundingMessage>>() {
                @Override
                public Single<? extends CrowdfundingMessage> call(Message message) {
                    return CrowdfundingManager.getInstance().saveCrowdfundingMessageToRealm(message);
                }
            })
            .flatMap(new Func1<CrowdfundingMessage, Single<? extends List<Message>>>() {
                @Override
                public Single<? extends List<Message>> call(CrowdfundingMessage crowdfundingMessage) {
                    return HttpApiManager.getCrowdfundingApi().getFeed(getAPIKey(), crowdfundingMessage.getTimestamp());
                }
            })
            .flatMap(new Func1<List<Message>, Single<? extends List<CrowdfundingMessage>>>() {
                @Override
                public Single<? extends List<CrowdfundingMessage>> call(List<Message> messages) {
                    return CrowdfundingManager.getInstance().saveCrowdfundingMessageToRealm(messages);
                }
            });
    }

    public static Single<List<CrowdfundingMessage>> getFeed(int timestamp) {
        return HttpApiManager.getCrowdfundingApi().getFeed(getAPIKey(), timestamp)
            .flatMap(new Func1<List<Message>, Single<? extends List<CrowdfundingMessage>>>() {
                @Override
                public Single<? extends List<CrowdfundingMessage>> call(List<Message> messages) {
                    return CrowdfundingManager.getInstance().saveCrowdfundingMessageToRealm(messages);
                }
            });
    }

    private static String getAPIKey() {
        return "APIKey " + Application.getInstance().getResources().getString(R.string.CROWDFUNDING_KEY);
    }

    public static class Message {
        private final String uuid;
        private final int timestamp;
        private final List<LocalizedMessage> feed;
        private final Author author;

        public Message(String uuid, int timestamp, List<LocalizedMessage> feed, Author author) {
            this.uuid = uuid;
            this.timestamp = timestamp;
            this.feed = feed;
            this.author = author;
        }

        public String getUuid() {
            return uuid;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public List<LocalizedMessage> getFeed() {
            return feed;
        }

        public Author getAuthor() {
            return author;
        }
    }

    public static class LocalizedMessage {
        private final String locale;
        private final String message;

        public LocalizedMessage(String locale, String message) {
            this.locale = locale;
            this.message = message;
        }

        public String getLocale() {
            return locale;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class Author {
        private final String avatar;
        private final String jabber_id;
        private List<LocalizedName> name;

        public Author(String avatar, String jabber_id, List<LocalizedName> name) {
            this.avatar = avatar;
            this.jabber_id = jabber_id;
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getJabberId() {
            return jabber_id;
        }

        public List<LocalizedName> getName() {
            return name;
        }
    }

    public static class LocalizedName {
        private final String locale;
        private final String name;

        public LocalizedName(String locale, String name) {
            this.locale = locale;
            this.name = name;
        }

        public String getLocale() {
            return locale;
        }

        public String getName() {
            return name;
        }
    }
}
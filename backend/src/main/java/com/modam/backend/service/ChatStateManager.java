package com.modam.backend.service;

import com.modam.backend.model.VoteStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChatStateManager {

    private final Map<Integer, DiscussionState> stateMap = new HashMap<>();

    public void initStateIfAbsent(int clubId) {
        stateMap.putIfAbsent(clubId, new DiscussionState());
    }

    public void setCurrentTopicIndex(int clubId, int index) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).setCurrentTopicIndex(index);
    }

    public int getCurrentTopicIndex(int clubId) {
        initStateIfAbsent(clubId);
        return stateMap.get(clubId).getCurrentTopicIndex();
    }

    public void raiseHand(int clubId, String userId) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).getRaisedHands().add(userId);
    }

    public Set<String> getRaisedHands(int clubId) {
        initStateIfAbsent(clubId);
        return stateMap.get(clubId).getRaisedHands();
    }

    public void clearRaisedHands(int clubId) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).getRaisedHands().clear();
    }

    public void setVote(int clubId, String userId, VoteStatus vote) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).getVoteStatusMap().put(userId, vote);
    }

    public Map<String, VoteStatus> getVoteStatusMap(int clubId) {
        initStateIfAbsent(clubId);
        return stateMap.get(clubId).getVoteStatusMap();
    }

    public void resetVotes(int clubId) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).getVoteStatusMap().clear();
    }

    public void updateLastMessageTime(int clubId) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).setLastMessageTimeMillis(System.currentTimeMillis());
    }

    public long getLastMessageTime(int clubId) {
        initStateIfAbsent(clubId);
        return stateMap.get(clubId).getLastMessageTimeMillis();
    }

    public void startTopicTimer(int clubId) {
        initStateIfAbsent(clubId);
        stateMap.get(clubId).setTopicStartTimeMillis(System.currentTimeMillis());
    }

    public long getTopicStartTime(int clubId) {
        initStateIfAbsent(clubId);
        return stateMap.get(clubId).getTopicStartTimeMillis();
    }

    public Set<Integer> getAllClubIds() {
        return stateMap.keySet();
    }

    @Getter
    @Setter
    private static class DiscussionState {
        private int currentTopicIndex = 0;
        private Set<String> raisedHands = new HashSet<>();
        private Map<String, VoteStatus> voteStatusMap = new HashMap<>();
        private long lastMessageTimeMillis = System.currentTimeMillis();
        private long topicStartTimeMillis = System.currentTimeMillis();
    }
}

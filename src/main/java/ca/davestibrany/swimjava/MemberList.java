package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemberList {
    private final Logger logger = LogManager.getLogger();
    private final Set<Member> members;
    private final Member self;

    MemberList(Set<Member> list, Member self) {
        members = list;
        this.self = self;
        members.add(self);
    }

    MemberList(Member self) {
        members = ConcurrentHashMap.newKeySet();
        this.self = self;
        members.add(self);
    }

    void add(Member m) {
        members.add(m);
    }

    boolean contains(Member m) {
        return members.contains(m);
    }

    List<Member> getAsList() {
        return new ArrayList<>(members);
    }

    int size() {
        return members.size();
    }

    void updateMemberState(Gossip gossip) {
        if (gossip.getGossipType() == GossipType.JOIN && !members.contains(gossip.getMember())) {
            members.add(gossip.getMember());
            logger.info("{} has joined", gossip.getMember());
            return;
        }

        Member member = get(gossip.getMember());
        if (member == null) return;

        if (gossip.getMember().getIncarnationNumber() > member.getIncarnationNumber()) {
            member.setIncarnationNumber(gossip.getMember().getIncarnationNumber());
        }

        switch (gossip.getGossipType()) {
            case ALIVE:
                logger.info("{} marking {} as ALIVE", self, gossip.getMember());
                member.alive();
                break;
            case SUSPECT:
                logger.info("{} marking {} as SUSPECT", self, gossip.getMember());
                member.suspect();
                break;
            case CONFIRM:
                logger.info("{} marking {} as DEAD", self, gossip.getMember());
                members.remove(member);
                break;
        }
    }

    List<Member> getRandomMembers(int k, Member excluded) {
        Random rand = new Random();
        List<Member> randomMembers = new ArrayList<>();
        List<Member> selectionList = getAsList();
        selectionList.remove(self);
        selectionList.remove(excluded);

        int selectionSize = Math.min(k, selectionList.size());
        for (int i = 0; i < selectionSize; i++) {
            int randomIndex = rand.nextInt(selectionList.size());
            randomMembers.add(selectionList.get(randomIndex));
            selectionList.remove(randomIndex);
        }

        return randomMembers;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        List<Member> memberList = getAsList();

        output.append("MemberList[");
        for (int i = 0; i < memberList.size(); i++) {
            output.append(memberList.get(i));
            if (i < memberList.size() - 1)
                output.append(", ");
        }
        output.append("]");

        return output.toString();
    }

    // TODO: this is O(n)
    private Member get(Member member) {
        return members.stream().filter(m -> m.equals(member)).findAny().orElse(null);
    }
}

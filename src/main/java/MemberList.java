import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
- update list
- remove from list
- reorder
- get k random
 */
public class MemberList {
    private final Set<Member> memberList;
    private final Member self;

    MemberList(Set<Member> list, Member self) {
        memberList = list;
        this.self = self;
        memberList.add(self);
    }

    MemberList(Member self) {
        memberList = ConcurrentHashMap.newKeySet();
        this.self = self;
        memberList.add(self);
    }

    void add(Member m) {
        memberList.add(m);
    }

    void remove(Member m) {
        memberList.remove(m);
    }

    boolean contains(Member m) {
        return memberList.contains(m);
    }

    List<Member> getAsList() {
        return new ArrayList<>(memberList);
    }

    public int size() {
        return memberList.size();
    }

    void updateMemberState(Gossip gossip) {
        if (gossip.getGossipType() == GossipType.JOIN) {
            memberList.add(gossip.getMember());
        }

        Member member = memberList.stream().filter(m -> m.equals(gossip.getMember())).findAny().orElse(null);
        if (member == null) return;

        if (gossip.getMember().getIncarnationNumber() > member.getIncarnationNumber()) {
            member.setIncarnationNumber(gossip.getMember().getIncarnationNumber());
        }

        switch (gossip.getGossipType()) {
            case ALIVE:
                member.alive();
                break;
            case SUSPECT:
                member.suspect();
                break;
            case CONFIRM:
                memberList.remove(member);
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
}

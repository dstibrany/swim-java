import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Disseminator {
    private List<Member> memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer();

    Disseminator(List<Member> memberList) {
        this.memberList = memberList;
    }

    List<Gossip> generateGossip() {
        return new ArrayList<>();
    }

    List<Gossip> sendMemberList() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member member : memberList) {
            gossipList.add(new Gossip(GossipType.JOIN, member, 0));
        }
        return gossipList;
    }

    void mergeGossip(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {

            boolean merged = gossipBuffer.mergeItem(gossip);
            if (merged) {
                Member member = new Member(1234, InetAddress.getLoopbackAddress());
//                Member member = memberList.contains(gossip.getMember()) ? memberList.get(memberList.indexOf(gossip.getMember())) : null;
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
                    case JOIN:
                        memberList.add(gossip.getMember());
                        break;
                }
            }
        }
    }

}

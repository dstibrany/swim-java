import java.util.ArrayList;
import java.util.List;

/*

On ALIVE:
 if member exists:
   add gossip to gossip list
   unsuspect(member)
 else:
   add member to memberlist

   add gossip to gossip list

On Confirm:
  remove member from memberlist
  add gossip

On Suspect
mark member as suspect
add gossip




 */

public class Disseminator {
    private List<Member> memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer();

    Disseminator(List<Member> memberList) {
        this.memberList = memberList;
    }

    List<Gossip> generateGossip() {
        return new ArrayList<>();
    }

    List<Gossip> generateJoinGossip() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member m : memberList) {
            gossipList.add(new Gossip(GossipType.ALIVE, m, 0));
        }
        return gossipList;
    }

    void mergeGossip(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            gossipBuffer.mergeItem(gossip);
        }
    }

}

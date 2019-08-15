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
        for (Gossip g : gossipList) {
            switch (g.getGossipType()) {
                case ALIVE:
                    memberList.add(g.getMember());
                    break;
                case SUSPECT:
                    break;
                case CONFIRM:
                    break;
                default:
                    break;
            }
        }
    }

}

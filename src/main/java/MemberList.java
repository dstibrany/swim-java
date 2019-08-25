import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/*
- update list
- remove from list
- reorder
- get k random
 */
public class MemberList {
    private final Set<Member> memberList;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock rLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock wLock = lock.writeLock();

    MemberList(List<Member> list) {
        memberList = new HashSet<>(list);
    }

    MemberList() {
        memberList = new HashSet<>();
    }

    void add(Member m) {
        wLock.lock();
        try {
            memberList.add(m);
        } finally {
            wLock.unlock();
        }
    }

    void remove(Member m) {
        wLock.lock();
        try {
            memberList.remove(m);
        } finally {
            wLock.unlock();
        }
    }

    boolean contains(Member m) {
        rLock.lock();
        try {
            return memberList.contains(m);
        } finally {
            rLock.unlock();
        }
    }

    Member get(Member member) {
        rLock.lock();
        try {
            return memberList.stream().filter(member::equals).findAny().orElse(null);
        } finally {
            rLock.unlock();
        }
    }

    List<Member> getList() {
        rLock.lock();
        try {
            return new ArrayList<>(memberList);
        } finally {
            rLock.unlock();
        }
    }

    int size() {
        return memberList.size();
    }

}

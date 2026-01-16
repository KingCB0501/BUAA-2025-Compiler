package llvm;

public class Use {
    private User user;
    private Value used;    //
    // user使用used

    public Use(User user, Value used) {
        this.user = user;
        this.used = used;
    }

    public Value getUsed() {
        return used;
    }

    public User getUser() {
        return user;
    }
}

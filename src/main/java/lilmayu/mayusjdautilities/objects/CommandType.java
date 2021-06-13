package lilmayu.mayusjdautilities.objects;

public enum CommandType {

    GENERAL,
    FUN,
    CRYPTO,
    MODERATION;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

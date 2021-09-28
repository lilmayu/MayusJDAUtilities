package dev.mayuna.mayusjdautils.commands.types;

public abstract class BaseCommandType {

    public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof BaseCommandType))
            return false;

        BaseCommandType that = (BaseCommandType) o;
        return getName().equals(that.getName());
    }
}

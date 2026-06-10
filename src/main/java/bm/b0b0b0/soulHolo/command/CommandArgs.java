package bm.b0b0b0.soulHolo.command;

public final class CommandArgs {

    private CommandArgs() {
    }

    public static int parseLine(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public static String join(String[] args, int from) {
        StringBuilder builder = new StringBuilder();
        for (int index = from; index < args.length; index++) {
            if (index > from) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }
}

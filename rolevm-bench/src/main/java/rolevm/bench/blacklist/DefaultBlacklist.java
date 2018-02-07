package rolevm.bench.blacklist;

class DefaultBlacklist implements Blacklist {

    public static class One implements Blacklist {
        private final String prefix;

        public One(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean isExcluded(final String name) {
            return name.startsWith(prefix);
        }
    }

    public static class Two implements Blacklist {
        private final String prefix1, prefix2;

        public Two(final String prefix1, final String prefix2) {
            this.prefix1 = prefix1;
            this.prefix2 = prefix2;
        }

        @Override
        public boolean isExcluded(final String name) {
            return name.startsWith(prefix1) || name.startsWith(prefix2);
        }
    }

    public static class Three implements Blacklist {
        private final String prefix1, prefix2, prefix3;

        public Three(final String prefix1, final String prefix2, final String prefix3) {
            this.prefix1 = prefix1;
            this.prefix2 = prefix2;
            this.prefix3 = prefix3;
        }

        @Override
        public boolean isExcluded(final String name) {
            return name.startsWith(prefix1) || name.startsWith(prefix2) || name.startsWith(prefix3);
        }
    }

    private final String[] prefixes;

    DefaultBlacklist(final String... prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public boolean isExcluded(String name) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix))
                return true;
        }
        return false;
    }
}

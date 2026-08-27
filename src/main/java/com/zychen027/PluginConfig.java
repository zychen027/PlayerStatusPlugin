package com.zychen027;

/**
 * Configuration POJO - automatically saved/loaded as JSON by ZenithProxy.
 * All fields public and mutable.
 */
public class PluginConfig {
    public final QueueWarnConfig queueWarn = new QueueWarnConfig();

    public static class QueueWarnConfig {
        public boolean enabled = true;
        /** keyword to match in the on-screen title/subtitle */
        public String keyword = "Position in queue: ";
        /** log at warn level when the extracted position is <= this */
        public int threshold = 100;
    }
}

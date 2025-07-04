package com.example.partymaker.data.firebase;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a snapshot of data from the server
 * This is a wrapper around the data returned from the server
 */
public class ServerDataSnapshot {
    private final Object data;
    private final String path;

    /**
     * Constructor
     * @param data The data from the server
     */
    public ServerDataSnapshot(Object data) {
        this.data = data;
        this.path = "";
    }

    /**
     * Constructor
     * @param path The path of the data
     * @param data The data from the server
     */
    public ServerDataSnapshot(String path, Object data) {
        this.path = path;
        this.data = data;
    }

    /**
     * Get the value of the snapshot
     * @return The value
     */
    public Object getValue() {
        return data;
    }

    /**
     * Get the value of the snapshot as a specific type
     * @param <T> The type to cast to
     * @param valueType The class of the type
     * @return The value cast to the specified type
     */
    public <T> T getValue(Class<T> valueType) {
        if (data == null) {
            return null;
        }
        
        if (valueType.isInstance(data)) {
            return valueType.cast(data);
        }
        
        return null;
    }

    /**
     * Get the path of the snapshot
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Check if the snapshot has a specific child
     * @param childPath The path of the child
     * @return True if the child exists, false otherwise
     */
    public boolean hasChild(String childPath) {
        if (data instanceof Map) {
            return ((Map<?, ?>) data).containsKey(childPath);
        }
        return false;
    }

    /**
     * Get a child snapshot
     * @param childPath The path of the child
     * @return The child snapshot
     */
    public ServerDataSnapshot child(String childPath) {
        if (data instanceof Map) {
            Object childData = ((Map<?, ?>) data).get(childPath);
            return new ServerDataSnapshot(childData);
        }
        return new ServerDataSnapshot(null);
    }

    /**
     * Get all children of the snapshot
     * @return A map of child snapshots
     */
    public Map<String, ServerDataSnapshot> getChildren() {
        Map<String, ServerDataSnapshot> children = new HashMap<>();
        
        if (data instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) data).entrySet()) {
                if (entry.getKey() instanceof String) {
                    String key = (String) entry.getKey();
                    children.put(key, new ServerDataSnapshot(entry.getValue()));
                }
            }
        }
        
        return children;
    }

    /**
     * Check if the snapshot exists
     * @return True if the snapshot exists, false otherwise
     */
    public boolean exists() {
        return data != null;
    }

    /**
     * Get the key of the snapshot
     * @return The key
     */
    public String getKey() {
        // The key is the last part of the path
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(lastSlashIndex + 1);
        }
        
        return path;
    }

    @Override
    public String toString() {
        return "ServerDataSnapshot{" +
                "path='" + path + '\'' +
                ", data=" + data +
                '}';
    }
} 
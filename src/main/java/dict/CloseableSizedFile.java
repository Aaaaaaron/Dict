package dict;

public interface CloseableSizedFile {
    long getSizeInBytes();

    void init();

    void close();
}

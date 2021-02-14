package kiruto.compress;

public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);


    public static Compress getByCode(int code) {
        switch (code) {
            case 1:
                return new GzipCompress();
        }
        return null;
    }

}

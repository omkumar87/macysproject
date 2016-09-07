package ex.com.filescanlister;

/**
 * Created by administrator on 9/6/16.
 */

class FileKrumb {

    int count;
    String ext;
    String name;
    long length;
    boolean isAverage = false;
    FileKrumb(String name,long len, int cnt,String ext){
        this.ext = ext;
        this.count = cnt;
        this.name = name;
        this.length = len;
    }

}
package indi.nonoas.worktools.pojo.params;

import cn.hutool.db.Entity;

/**
 * @author Nonoas
 * @datetime 2022/7/16 21:50
 */
public class MyEntity extends Entity {

    public MyEntity(String tableName) {
        super(tableName);
    }

    public void whereNonNull(String field, Object val) {
        if (val != null) {
            set(field, val);
        }
    }

    public void likeNonNull(String field, Object val) {
        if (val != null) {
            set(field, "like %" + val + "%");
        }
    }

    public static MyEntity create(String tableName) {
        return new MyEntity(tableName);
    }
}

package indi.nonoas.worktools.pojo.params;

/**
 * @author Nonoas
 * @date 2022/6/16
 */
public class PageQry {

    private int pageNo;
    private int pageSize;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getOffset() {
        return pageNo * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }
}

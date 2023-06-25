package lost.pikpak.client.bindata;

abstract class BinDataBase<S> implements BinData<S> {
    private final long len;
    private boolean consumed = false;

    BinDataBase(long len) {
        this.len = len;
    }

    @Override
    public boolean consumed() {
        return this.consumed;
    }

    @Override
    public void makeConsumed() {
        this.consumed = true;
    }

    @Override
    public long len() {
        return this.len;
    }
}

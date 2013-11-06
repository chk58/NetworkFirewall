package chk.android.networkfirewall;
public class NoPermissionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -8835242569001345496L;
    private final String mError;

    public NoPermissionException(String error) {
        mError = error;
    }

    public NoPermissionException(Throwable t) {
        if (t != null) {
            mError = t.toString();
        } else {
            mError = null;
        }
    }

    @Override
    public String toString() {
        return mError;
    }
}

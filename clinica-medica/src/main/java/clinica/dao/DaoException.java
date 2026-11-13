package clinica.dao;

public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

package bean.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import bean.CurrentUser;

import bean.dao.CurrentUserDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig currentUserDaoConfig;

    private final CurrentUserDao currentUserDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        currentUserDaoConfig = daoConfigMap.get(CurrentUserDao.class).clone();
        currentUserDaoConfig.initIdentityScope(type);

        currentUserDao = new CurrentUserDao(currentUserDaoConfig, this);

        registerDao(CurrentUser.class, currentUserDao);
    }
    
    public void clear() {
        currentUserDaoConfig.clearIdentityScope();
    }

    public CurrentUserDao getCurrentUserDao() {
        return currentUserDao;
    }

}

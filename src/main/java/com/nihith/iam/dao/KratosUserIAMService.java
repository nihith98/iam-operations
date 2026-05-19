package com.nihith.iam.dao;

import com.nihith.iam.exception.IAMException;
import com.nihith.iam.interfaces.UserIAMService;
import com.nihith.iam.model.User;
import com.nihith.iam.util.EnvironmentUtil;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

/**
 * Ory Kratos-backed implementation of {@link UserIAMService}. Delegates user
 * identity operations to a Kratos instance via its REST APIs (Admin for
 * create/update/delete, Public for login flows). Token and role storage are
 * unaffected and continue to use MongoDB via {@code TokenMongoDao}.
 *
 * <p>This class is a stub that wires up the HTTP client and reads the Kratos URLs
 * from the environment. Endpoint implementations are added incrementally as each
 * REST flow is built out.</p>
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class KratosUserIAMService implements UserIAMService {

    private static final Logger logger = LogManager.getLogger(KratosUserIAMService.class);

    public static final String KRATOS_ADMIN_URL = "KRATOS_ADMIN_URL";
    public static final String KRATOS_PUBLIC_URL = "KRATOS_PUBLIC_URL";

    private String kratosAdminUrl;
    private String kratosPublicUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Reads the Kratos Admin and Public API base URLs from the environment.
     */
    @PostConstruct
    public void init() {
        this.kratosAdminUrl = EnvironmentUtil.getEnvironmentVariable(KRATOS_ADMIN_URL);
        this.kratosPublicUrl = EnvironmentUtil.getEnvironmentVariable(KRATOS_PUBLIC_URL);
        logger.info("Kratos Admin URL::{}", this.kratosAdminUrl);
        logger.info("Kratos Public URL::{}", this.kratosPublicUrl);
    }

    /**
     * {@inheritDoc}
     * <p>Not yet implemented — Kratos backend wiring is staged for a later phase.</p>
     */
    @Override
    public boolean createUser(User user) throws IAMException {
        logger.info("Entered createUser (Kratos backend)");
        throw new IAMException("Kratos createUser is not yet implemented");
    }

    /**
     * {@inheritDoc}
     * <p>Not yet implemented — Kratos backend wiring is staged for a later phase.</p>
     */
    @Override
    public User findByUsername(String username) throws IAMException {
        logger.info("Entered findByUsername (Kratos backend)");
        throw new IAMException("Kratos findByUsername is not yet implemented");
    }

    /**
     * {@inheritDoc}
     * <p>Not yet implemented — Kratos backend wiring is staged for a later phase.</p>
     */
    @Override
    public User findByUserId(String userId) throws IAMException {
        logger.info("Entered findByUserId (Kratos backend)");
        throw new IAMException("Kratos findByUserId is not yet implemented");
    }

    /**
     * {@inheritDoc}
     * <p>Deferred to Phase 2 (Kratos integration). Currently throws an exception
     * indicating that Kratos support for registration is not yet implemented.</p>
     */
    @Override
    public boolean usernameExists(String username) throws IAMException {
        logger.info("Entered usernameExists (Kratos) — not yet implemented");
        throw new IAMException("Kratos registration support deferred to Phase 2");
    }
}

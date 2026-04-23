INSERT INTO app_user (id, username, password, enabled)
VALUES (1, 'admin', '$2a$10$3Lt7be5w/QD0UA56O4tsX./r3K.bU1ELzg10NCyyrKSI20F2GrKby', true),
       (2, 'user1', '$2a$10$bmvhgpoetBgo.vBNk/6ts.x7kOuJ5.kJy8LmMcn6F/sGKJMLS1oDu', true),
       (3, 'user2', '$2a$10$bmvhgpoetBgo.vBNk/6ts.x7kOuJ5.kJy8LmMcn6F/sGKJMLS1oDu', true);

INSERT INTO role (id, name)
VALUES (1, 'ADMIN'),
       (2, 'USER');

INSERT INTO permission (id, name)
VALUES (1, 'MANAGE_ROLES'),
       (2, 'MANAGE_PERMISSIONS'),
       (3, 'ASSIGN_PERMISSIONS'),
       (4, 'ASSIGN_ROLES'),
       (5, 'ACCESS_SECURE_DATA');

INSERT INTO role_permission (id, role_id, permission_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 1, 4);

INSERT INTO role_permission (id, role_id, permission_id)
VALUES (5, 2, 5);

INSERT INTO user_role (id, user_id, role_id)
VALUES (1, 1, 1),
       (2, 2, 2);

ALTER TABLE app_user
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE role
    ALTER COLUMN id RESTART WITH 3;
ALTER TABLE permission
    ALTER COLUMN id RESTART WITH 6;
ALTER TABLE role_permission
    ALTER COLUMN id RESTART WITH 6;
ALTER TABLE user_role
    ALTER COLUMN id RESTART WITH 3;

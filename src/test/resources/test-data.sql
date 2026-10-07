DELETE FROM audit_logs; DELETE FROM mfa_codes; DELETE FROM user_roles; DELETE FROM users;
DELETE FROM role_permissions; DELETE FROM permission_applications; DELETE FROM roles; DELETE FROM permissions; DELETE FROM applications;
INSERT INTO roles(id,name) VALUES(1,'EMPLOYEE'),(2,'HR'),(3,'MANAGER'),(4,'ADMIN');
INSERT INTO permissions(id,name) VALUES(1,'USER_VIEW'),(2,'USER_MANAGE'),(3,'HR_VIEW'),(4,'HR_MANAGE'),(5,'ATTENDANCE_VIEW'),(6,'ATTENDANCE_MANAGE'),(7,'INVENTORY_VIEW'),(8,'INVENTORY_MANAGE'),(9,'AUDIT_VIEW');
INSERT INTO role_permissions VALUES(1,3),(1,5),(1,7),(2,1),(2,3),(2,4),(2,5),(3,1),(3,3),(3,5),(3,7),(4,1),(4,2),(4,3),(4,4),(4,5),(4,6),(4,7),(4,8),(4,9);
INSERT INTO applications(id,name,description) VALUES(1,'HR','People access demo'),(2,'ATTENDANCE','Attendance access demo'),(3,'INVENTORY','Inventory access demo');
INSERT INTO permission_applications VALUES(3,1),(4,1),(5,2),(6,2),(7,3),(8,3);

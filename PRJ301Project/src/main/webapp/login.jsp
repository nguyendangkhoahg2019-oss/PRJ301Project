<%-- 
    Document   : login
    Created on : Mar 10, 2026, 1:00:47 AM
    Author     : Dell
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
    </head>
    <body>
        <form action="auth?action=login" method="post">

            Username
            <input type="text" name="username">

            Password
            <input type="password" name="password">

            <button type="submit">Login</button>

        </form>
    </body>
</html>

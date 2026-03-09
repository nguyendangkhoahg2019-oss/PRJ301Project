<%-- 
    Document   : register
    Created on : Mar 10, 2026, 1:00:05 AM
    Author     : Dell
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Register</title>
    </head>
    <body>
        <form action="auth?action=register" method="post">

            <input name="username">
            <input name="password">
            <input name="fullname">
            <input name="email">
            <input name="phone">

            <button type="submit">Register</button>

        </form>
    </body>
</html>

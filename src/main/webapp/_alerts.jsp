<%@ page pageEncoding="UTF-8" %>
<%
    String alertMessage = (String) session.getAttribute("message");
    if (alertMessage != null) {
        session.removeAttribute("message");
    }
    String alertError = (String) request.getAttribute("error");
%>
<% if (alertMessage != null) { %>
    <div class="alert <%= alertMessage.contains("成功") ? "alert-success" : "alert-error" %>">
        <%= alertMessage %>
    </div>
<% } %>
<% if (alertError != null) { %>
    <div class="alert alert-error"><%= alertError %></div>
<% } %>

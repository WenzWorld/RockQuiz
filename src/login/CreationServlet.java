package login;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletContext;

/**
 * Servlet implementation class CreationServlet
 */
@WebServlet("/CreationServlet")
public class CreationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreationServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		String usrname = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		
		ServletContext servContext = getServletConfig().getServletContext();
		AccountManager accnt = (AccountManager)servContext.getAttribute("accntManager");
		
		if (usrname == "" || pwd == "") {
			RequestDispatcher dispatch = request.getRequestDispatcher("createAccount.html");
			dispatch.forward(request, response);
			return;
		} 
		
		if (accnt.alreadyExist(usrname)) {
			RequestDispatcher dispatch = request.getRequestDispatcher("nameInUse.jsp");
			dispatch.forward(request, response);
		} else {
			accnt.createNewAccount(usrname, pwd);
			String usrpage = "userpage.jsp?id=" + usrname;
			RequestDispatcher dispatch = request.getRequestDispatcher(usrpage);
			dispatch.forward(request, response);
		}
			
		
	}

}

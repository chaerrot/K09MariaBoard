<%@page import="utils.BoardPage"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="model1.board.BoardDAO"%>
<%@page import="model1.board.BoardDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//DAO 객체 생성 및 DB연결
	BoardDAO dao = new BoardDAO(application);

	//검색어가 있는 경우 파라미터를 저장하기 위한 Map컬렉션 생성
	Map<String, Object> param = new HashMap<String, Object>();
	//검색 파라미터를 request 내장 객체를 통해 얻어온다.
	String searchField = request.getParameter("searchField");
	String searchWord = request.getParameter("searchWord");
	//검색어가 있는 경우에만..
	if (searchWord != null) {
		//Map컬렉션에 파라미터 값을 추가한다.
		param.put("searchField", searchField); //검색필드명(title, content 등)
		param.put("searchWord", searchWord); //검색어
	}
	//board테이블에 저장된 게시물의 개수 카운트
	int totalCount = dao.selectCount(param);
	
	
	//페이지 처리를 위한 코드 추가 부분........
	/*** 페이지 처리 start ***/
	//컨텍스트 초기화 파라미터를 얻어 온 후 사칙연산을 위해 정수로 변경한다.
	int pageSize = Integer.parseInt(application.getInitParameter("POSTS_PER_PAGE"));
	int blockPage = Integer.parseInt(application.getInitParameter("PAGES_PER_BLOCK"));
	//전체 페이지 수를 계산한다.
	int totalPage = (int)Math.ceil((double)totalCount / pageSize);
	/*
		목록에 첫 진입 시에는 페이지 관련 파라미터가 없으므로 무조건 1page로 지정한다.
		만약 pageNum이 있다면 파라미터를 받아와서 정수로 변경한 후 페이지 수로 지정한다.
	*/
	int pageNum = 1;
	String pageTemp = request.getParameter("pageNum");
	if (pageTemp != null && !pageTemp.equals(""))
		pageNum = Integer.parseInt(pageTemp);
	
	//게시물의 구간을 계산한다.
	//int start = (pageNum - 1) * pageSize + 1; //구간의 시작
	//int end = pageNum * pageSize; //구간의 끝
	/*
		한 페이지에 출력할 게시물의 범위를 결정한다. MySQL 에서는 limit를
		사용하므로 start(시작위치)만 계산하면 된다.
			start=>
				1페이지일 때: (1-1) * 5 = 0
				2페이지일 때: (2-1) * 5 = 5
			end=>
				한 페이지 출력할 게시물의 개수인 pageSize를 그대로
				사용하면 된다.
	*/
	int start = (pageNum - 1) * pageSize;
	int end = pageSize;
	
	
	param.put("start", start); //Map컬렉션에 저장 후 DAO로 전달함.
	param.put("end", end);
	/*** 페이지 처리 end ***/
	
	
	//출력할 레코드 추출
	List<BoardDTO> boardLists = dao.selectListPage(param);
	//자원 해제
	dao.close();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원제 게시판</title>
</head>
<body>
    <jsp:include page="../Common/Link.jsp" /> 

    <h2>목록 보기(List) - 현재 페이지: <%= pageNum %> (전체: <%= totalPage %>)</h2>
    
    <form method="get">  
    <table border="1" width="90%">
    <tr>
        <td align="center">
            <select name="searchField"> 
                <option value="title">제목</option> 
                <option value="content">내용</option>
            </select>
            <input type="text" name="searchWord" />
            <input type="submit" value="검색하기" />
        </td>
    </tr>   
    </table>
    </form>
  
    <table border="1" width="90%">
        <!-- 각 칼럼의 이름 --> 
        <tr>
            <th width="10%">번호</th>
            <th width="50%">제목</th>
            <th width="15%">작성자</th>
            <th width="10%">조회수</th>
            <th width="15%">작성일</th>
        </tr>
<%
	if (boardLists.isEmpty()) {
		//게시물이 하나도 없을 때
%>
		<tr>
			<td colspan="5" align="center">
				등록된 게시물이 없습니다!^^
			</td>
		</tr>
<%
	}
	else {
		//게시물이 있을 때
		int virtualNum = 0; //게시물의 출력번호
		int countNum = 0;
		//확장 for문을 통해 List컬렉션에 저장된 레코드 개수만큼 반복한다.
		for (BoardDTO dto : boardLists) {
			virtualNum = totalCount - (((pageNum - 1) * pageSize) + countNum++);
%>
		<tr align="center">
			<td><%= virtualNum %></td>
			<td align="left">
				<a href="View.jsp?num=<%= dto.getNum() %>"><%= dto.getTitle() %></a>
			</td>
			<td align="center"><%= dto.getId() %></td>
			<td align="center"><%= dto.getVisitcount() %></td>
			<td align="center"><%= dto.getPostdate() %></td>
		</tr>
<%
		}
	}
%>
    </table>
    <table border="1" width="90%">
        <tr align="center">
        	<!-- 페이징 처리 -->
        	<td>
        		<%= BoardPage.pagingStr(totalCount, pageSize,
        				blockPage, pageNum, request.getRequestURI(), param) %>
        		<!--
        			request.getRequestURI(): request내장 객체를 통해 현재 페이지에서
        				HOST부분을 제외한 전체 경로명을 얻을 수 있다. 여기서 얻은
        				경로명을 통해 "경로명?pageNum=번호"와 같은 링크를 만들 수 있다.
        		  -->
        	</td>
            <!-- 글쓰기 버튼 -->
            <td><button type="button" onclick="location.href='Write.jsp';">글쓰기
                </button></td>
        </tr>
    </table>
</body>
</html>

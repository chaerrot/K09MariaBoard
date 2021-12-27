package model2.mvcboard;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.DBConnPool;
import model1.board.BoardDTO;

public class MVCBoardDAO extends DBConnPool {

	public MVCBoardDAO() {
		super();
	}
	
	public int selectCount (Map<String, Object> map) {
		//카운트 변수
		int totalCount = 0;
		//쿼리문 작성
		String query = "SELECT COUNT(*) FROM mvcboard";
		//검색어가 있는 경우 where절을 동적으로 추가한다.
		if (map.get ("searchWord") != null) {
			query += " WHERE " + map.get("searchField") + " "
						+ " LIKE '%" + map.get("searchWord") +"%'";
		}
		
		try {
			//정적쿼리문(?가 없는 쿼리문) 실행을 위한 Statement객체 생성
			stmt = con.createStatement();
			//select 쿼리문을 실행 후 ResultSet 객체를 반환 받음
			rs = stmt.executeQuery(query);
			//커서를 이동시켜 결과데이터를 읽음
			rs.next();
			//결과값을 변수에 저장
			totalCount = rs.getInt(1);
		}
		catch (Exception e ) {
			System.out.println("게시물 수를 구하는 중 예외 발생");
			e.printStackTrace();
		}
		
		return totalCount;
	}
	
	public List <MVCBoardDTO> selectListPage(Map<String, Object> map) {
		/*
		 board테이블에서 select한 결과데이터를 저장하기 위한 리스트 컬렉션.
		 여러 가지의 List컬렉션 중 동기화가 보장되는 Vector를 사용한다.
		 */
		List<MVCBoardDTO> board = new Vector<MVCBoardDTO>();
		
		/*
		 목록에 출력할 게시물을 추출하기 위한 쿼리문으로 항상 일련번호의
		 역순(내림차순)으로 정렬해야 한다. 게시판의 목록은 최근 게시물이
		 제일 앞에 노출되기 때문이다.
		 */
		String query = " SELECT * FROM mvcboard ";
		
		if (map.get("searchWord") != null) {
			query += " WHERE " + map.get("searchField") +" "
						+ " LIKE '%" + map.get("searchWord") +"%' ";
		}
		query += " ORDER BY idx DESC LIMIT ?, ? ";
		
		try {
			psmt = con.prepareStatement(query);
			psmt.setInt(1, Integer.parseInt(map.get("start").toString()));
			psmt.setInt(2, Integer.parseInt(map.get("end").toString()));
			rs = psmt.executeQuery();
			
			//추출된 결과에 따라 반복한다.
			while (rs.next()) {
				//하나의 레코드를 읽어서 DTO객체에 저장한다.
				MVCBoardDTO dto = new MVCBoardDTO();
				
				dto.setIdx(rs.getString(1));
				dto.setName(rs.getString(2));
				dto.setTitle(rs.getString(3));
				dto.setContent(rs.getString(4));
				dto.setPostdate(rs.getDate(5));
				dto.setOfile(rs.getString(6));
				dto.setSfile(rs.getString(7));
				dto.setDowncount(rs.getInt(8));
				dto.setPass(rs.getString(9));
				dto.setVisitcount(rs.getInt(10));
				
				//리스트 컬렉션에 DTO객체를 추가한다.
				board.add(dto);
			}
		}
		catch (Exception e) {
			System.out.println("게시물 조회 중 예외 발생");
			e.printStackTrace();
		}
		
		return board;
	}
	
	//새로운 게시물에 대한 입력 처리
	public int insertWrite(MVCBoardDTO dto) {
		int result = 0;
		try {
			String query = "INSERT INTO mvcboard ( "
						+ " name, title, content, ofile, sfile, pass) "
						+ " VALUES ( "
						+ " ?, ?, ?, ?, ?, ?)";
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getName());
			psmt.setString(2, dto.getTitle());
			psmt.setString(3, dto.getContent());
			psmt.setString(4, dto.getOfile());
			psmt.setString(5, dto.getSfile());
			psmt.setString(6, dto.getPass());
			result = psmt.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("게시물 입력 중 예외 발생");
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	//주어진 일련번호에 해당하는 게시물을 DTO에 담아 반환한다.
	public MVCBoardDTO selectView (String idx) {
		MVCBoardDTO dto = new MVCBoardDTO(); //DTO 객체 생성
		String query = "SELECT * FROM mvcboard WHERE idx=? ";
		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, idx);
			rs = psmt.executeQuery();
			
			if (rs.next()) {
				//
				dto.setIdx(rs.getString(1));
				dto.setName(rs.getString(2));
				dto.setTitle(rs.getString(3));
				dto.setContent(rs.getString(4));
				dto.setPostdate(rs.getDate(5));
				dto.setOfile(rs.getString(6));
				dto.setSfile(rs.getString(7));
				dto.setDowncount(rs.getInt(8));
				dto.setPass(rs.getString(9));
				dto.setVisitcount(rs.getInt(10));		
			}
		}
		catch (Exception e) {
			System.out.println("게시물 상세보기 중 예외 발생");
			e.printStackTrace();
		}
		
		return dto;
	}
	
	//주어진 일련번호에 해당하는 게시물의 조회수를 1 증가시킨다.
	public void updateVisitCount (String idx) {
		String query = "UPDATE mvcboard SET "
					+ " visitcount=visitcount+1 "
					+ " WHERE idx=?";
		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, idx);
			psmt.executeQuery();
		}
		catch (Exception e) {
			System.out.println("게시물 증가 중 예외 발생");
			e.printStackTrace();
		}
	}
	
	//주어진 일련번호에 해당하는 게시물의 다운로드수 증가시킴
	public void downCountPlus (String idx) {
		String sql = "UPDATE mvcboard SET "
				+ " downcount=downcount+1 "
				+ " WHERE idx=? ";
		
		try {
			psmt = con.prepareStatement(sql);
			psmt.setString(1, idx);
			psmt.executeUpdate();
		}
		catch (Exception e) {}
	}
	
	//패스워드 검증을 위해 해당 게시물이 존재하는지 확인
	public boolean confirmPassword (String pass, String idx) {
		boolean isCorr = true;
		try {
			//패스워드와 일련번호를 통해 조건에 맞는 게시물이 있는지 확인
			String sql = "SELECT COUNT(*) FROM mvcboard WHERE pass=? AND idx=?";
			psmt = con.prepareStatement(sql);
			//인파라미터 설정
			psmt.setString(1, pass);
			psmt.setString(2, idx);
			rs = psmt.executeQuery();
			//커서 이동을 위한 next() 호출. count()함수는 항상 결과를 반환하므로
			//if문은 별도로 필요하지 않다.
			rs.next();
			if (rs.getInt(1) == 0) { //결과가 없을 때 false로 처리
				isCorr = false;
			}
		}
		catch (Exception e) {
			isCorr = false; //예외가 발생하면 확인이 안되므로 false로 처리
			e.printStackTrace();
		}
		return isCorr;
	}
	
	//일련번호에 해당하는 게시물 삭제
	public int deletePost (String idx) {
		int result = 0;
		try {
			String query = "DELETE FROM mvcboard WHERE idx=?";
			psmt = con.prepareStatement(query);
			psmt.setString(1, idx);
			result = psmt.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("게시물 삭제 중 예외 발생");
			e.printStackTrace();
		}
		return result;
	}
	
	//일련번호와 패스워드가 일치할 때만 게시물 업데이트 처리
	public int updatePost(MVCBoardDTO dto) {
		int result = 0;
		try {
			String query = "UPDATE mvcboard"
						+ " SET title=?, name=?, content=?, ofile=?, sfile=? "
						+ " WHERE idx=? and pass=?";
			
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getTitle());
			psmt.setString(2, dto.getName());
			psmt.setString(3, dto.getContent());
			psmt.setString(4, dto.getOfile());
			psmt.setString(5, dto.getSfile());
			psmt.setString(6, dto.getIdx());
			psmt.setString(7, dto.getPass());
			
			result = psmt.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("게시물 수정 중 예외 발생");
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	
}

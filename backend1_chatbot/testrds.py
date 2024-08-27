import pymysql
rds_host = 'db-ysu-006-campusdata.cdsomq8mgfd0.ap-south-1.rds.amazonaws.com'  # RDS 엔드포인트
db_username = 'admin'
db_password = 'password11!!'
db_name = 'CampDB'
query = "SELECT * FROM CampusProperty\nWHERE 건물번호 = '121'\nAND 층 = '1층'\nAND 용도 = '휴식'"
def query_rds(summarized_query):
    try:
    # RDS에 연결
        connection = pymysql.connect(
            host='db-ysu-006-campusdata.cdsomq8mgfd0.ap-south-1.rds.amazonaws.com',
            user='admin',
            password='password11!!',
            database='CampDB',
            cursorclass=pymysql.cursors.DictCursor
        )
        print("query_rds: Connected to RDS successfully")
        with connection.cursor() as cursor:
            cursor.execute(summarized_query)
            result = cursor.fetchone()
            print(f"query_rds: Query executed successfully, result: {result}")
            return result
            
    except Exception as e:
        print(f"query_rds: Error occurred: {str(e)}")
        raise
        
    finally:
        connection.close()

result = query_rds(query)
print(result)

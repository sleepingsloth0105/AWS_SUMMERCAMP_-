import json
import boto3
import pymysql
import logging

# AWS 서비스 클라이언트 생성
bedrock_client = boto3.client(service_name = 'bedrock-runtime', region_name ="us-east-1")  # Bedrock 클라이언트
rds_host = 'db-ysu-006-campusdata.cdsomq8mgfd0.ap-south-1.rds.amazonaws.com'  # RDS 엔드포인트
db_username = 'admin'
db_password = 'password11!!'
db_name = 'CampDB'
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    try:
        
        # event 객체 전체를 로그에 출력하여 구조를 확인
        logger.info(f"Event received: {json.dumps(event)}")

        # 'body' 키가 존재하는지 확인
        if 'body' not in event:
            logger.error("Error: 'body' key not found in event")
            return {
                'statusCode': 400,
                'body': json.dumps({
                    'message': "Bad Request: 'body' key not found"
                })
            }
        
        # 1. 클라이언트로부터 질문을 수신
        body = json.loads(event['body'])
        user_input = body.get('user_input', '')
        # assistant_input = body.get('assistant_input', '')
        logger.info(f"User input received: {user_input}")
        
        
        # 2. 사용자 정의 프롬프트 설정
        #bedrock_prompt = f"I want you to summarize the given question as the form of [place: purpose]. Question: {user_input}"
        bedrock_prompt = user_input
        
        
 
        # 3. 아마존 Bedrock을 호출하여 질문을 요약
        bedrock_response = call_bedrock(bedrock_prompt)
        query = bedrock_response['content'][0]['text']  # 예시: "장소: 화장실"
        logger.info(query)
        '''       
        
        # 배드록 관련 질문 처리에 성공시 성공 알림 반환
        second_response = {
            'statusCode': 200,
            'body': json.dumps({
                'message': 'BedRock question activated successfully'
            }),
            'headers': {
                'Content-Type': 'application/json',
            },
        }
        return second_response
        
        '''
        # 4. RDS에 쿼리 실행
        # query="[건물번호:121], [층:1층], [용도:휴식]" # 이부분은 예시로 만든 응답입니다
        sql = "SELECT * FROM CampusProperty\nWHERE "
        # 결과를 저장할 리스트
        pairs = []
        # 대괄호로 묶인 부분을 추출
        while '[' in query:
            # 대괄호의 시작과 끝 인덱스 찾기
            start_index = query.index('[')
            end_index = query.index(']')
        
            # 대괄호 안의 문자열 추출
            inside_brackets = query[start_index + 1:end_index].strip()
        
            # 추출한 부분에서 ':' 또는 ','로 key와 data 분리
            if ':' in inside_brackets:
                key, data = map(str.strip, inside_brackets.split(':', 1))
            elif ',' in inside_brackets:
                key, data = map(str.strip, inside_brackets.split(',', 1))
            else:
                key, data = inside_brackets, ''  # 데이터가 없는 경우
        
            # 추출된 쌍을 리스트에 추가
            pairs.append((key, data))
        
            # 문자열에서 처리한 부분 제거
            query = query[end_index + 1:].strip()
        for key, data in pairs:
            if key != '요약' and key != '용도':
                if data == '*':
                    data = 'IS NOT NULL'
                    sql += f"{key} {data}\nAND "
                else:
                    sql += f"{key} = '{data}'\nAND "
            elif key == '용도':
                if data == '*':
                    data = 'IS NOT NULL'
                    sql += f"{key} {data}\nAND "
                else:
                    sql += f"{key} LIKE '%{data}%'\nAND "
                purpose = data
            else:
                summarydata = data
        sql= sql[:-4]
        logger.info(sql)
        query_result = query_rds(sql,purpose)
        query_result.append(summarydata)
    
        # 5. 응답 생성
        response = {
            "isBase64Encoded" : True,
            'statusCode': 200,
            'body': json.dumps({
                'query_result': query_result
            }),
            'headers': {
                'Content-Type': 'application/json',
                "Access-Control-Allow-Origin": "*", 
                "Access-Control-Allow-Headers": "*",
                "Access-Control-Allow-Methods": "*",
            },
        }
        logger.info(f"response = {response}")
        return response
    
    except Exception as e:
        error_message = f"Error occurred: {str(e)}"
        print(error_message)
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)}),
            'headers': {
                'Content-Type': 'application/json',
            },
        }

def call_bedrock(prompt):
    inputcontent = []
    for input in prompt.split('\n'):
        inputcontent.append({ "type": "text", "text": input })
    logger.info(f"정리된 질문 = {inputcontent}")
    try:
    # Bedrock에 프롬프트 전달하여 요약 요청
        body = json.dumps(
            {
                "anthropic_version": "bedrock-2023-05-31",    
                "max_tokens": 200,
                "system": "You are a person who organizes questions received in a specific format without relying on prior knowledge.Do not use any of the knowledge you have about Yonsei University.answer my question in format i provide. fotamt is [건물번호:string], [층:string], [용도:string], [이름:string],[요약:string]. question is korean. if building name is 1공학관 or 4공학관 건물번호 is '121' else if name is 2공학관 answer is '122' else if name is 3공학관 answer is '123'. if no building name in question, give answer '*'.answer for '용도' you shoud take answer in ['휴식', '공부', '식사', '강의실','연구실','실험실','남자화장실','여자화장실','엘리베이터', '계단','*']. If you can't find the content in the '용도' category in your question give '*'. and '이름' is name for room in building. like before case, '이름' and '층' give '*'. but if '용도' is ['남자화장실', '여자화장실', '엘리베이터', '계단'] and floor is not mentioned in question, give '층' 1.finally '요약' is korean summary of question in 20 token. and you only take answer for last question. you may take two type of question. one is asking for recommand place. example of first case is t[1공학관 근처 밥먹을 수 있는 공간을 알려줘], your answer is '[건물번호:121], [층:*], [용도:휴식], [이름:*], [요약:1공학관 근처 식사 공간]'.another example for first case is [공학관 지하 1층에 쉴 수 있는 공간이 있나?] and your answer is '[건물번호:121], [층:b1], [용도:휴식], [이름:*], [요약:1공학관 근처 식사 공간]' and other type is ask for find place. example is [D405는 어디있어] and answer is like '[건물번호:*], [층:*], [용도:*], [이름:D405], [요약:D405의 위치 찾기]'. and another example for case 2 is [a302?] and your answer is '[건물번호:*], [층:*], [용도:*], [이름:A302], [요약:A302의 위치 찾기]'",
                "messages": [
                    {
                        "role": "user",
                        "content": inputcontent
                    }
                ],
                "temperature": 0,
            }
        )
        response = bedrock_client.invoke_model(
            modelId="anthropic.claude-3-5-sonnet-20240620-v1:0",  # 사용할 Bedrock 모델 ID
            body=body,
        )
        response_body = json.loads(response['body'].read().decode('utf-8'))
        logger.info(f"call_bedrock: Response body from Bedrock: {response_body}")
        return response_body
    except Exception as e:
        print(f"call_bedrock: Error occurred: {str(e)}")
        raise

def query_rds(summarized_query,purpose):
    try:
    # RDS에 연결
        connection = pymysql.connect(
            host='db-ysu-006-campusdata.cdsomq8mgfd0.ap-south-1.rds.amazonaws.com',
            user='admin',
            password='password11!!',
            database='CampDB',
            cursorclass=pymysql.cursors.DictCursor
        )
        logger.info("query_rds: Connected to RDS successfully")
    
        fetchmanypurpose = ['남자화장실', '여자화장실', '엘리베이터', '계단']
        with connection.cursor() as cursor:
            cursor.execute(summarized_query)
            if purpose not in fetchmanypurpose:
                result = cursor.fetchmany(3)
                result.append(0) # 3d map을 세 개 띄워야 하는 경우를 의미
            else:
                result = cursor.fetchall()
                result.append(1) # 하나의 3d map 위에 여러 개의 오버레이를 띄워야 하는 이유
            logger.info(f"query_rds: Query executed successfully, result: {result}")
            return result
            
    except Exception as e:
        print(f"query_rds: Error occurred: {str(e)}")
        raise
        
    finally:
        connection.close()
# 📘 Структура таблиц информационной системы ВУЗа для программы SmartCampus

## 🧑‍🎓 Студенты (Students)
| Поле               | Тип            | Комментарий                        |
|--------------------|----------------|------------------------------------|
| id                 | int PK         | Уникальный идентификатор           |
| surname            | nvarchar(255)  | Фамилия                            |
| name               | nvarchar(255)  | Имя                                |
| lastname           | nvarchar(255)  | Отчество                           |
| birthday           | date           | Дата рождения                      |
| group_id           | int FK         | Ссылка на таблицу `Groups`         |
| email              | nvarchar(255)  | Электронная почта                  |
| phone_number       | nvarchar(50)   | Номер телефона                     |
| photo              | varbinary(MAX) | Фото студента                      |

### Студенты — Дополнительная информация (Students_Info)
| Поле                | Тип            | Комментарий                        |
|---------------------|----------------|------------------------------------|
| student_id          | int PK, FK     | Ссылка на таблицу `Students`       |
| address             | ntext          | Адрес проживания                   |
| passport_number     | nvarchar(100)  | Паспорт                            |
| school              | nvarchar(255)  | Средняя школа                      |
| document_number     | nvarchar(100)  | Документ об образовании            |
| military            | nvarchar(100)  | Воинская обязанность               |
| student_card_number | nvarchar(100)  | Номер студенческого билета         |
| study_type          | nvarchar(100)  | Тип обучения (бюджет/платное)      |
| study_form          | nvarchar(100)  | Форма обучения (очная/заочная)     |
| status              | nvarchar(100)  | Статус (обучается/отчислен)        |
| father_fio          | nvarchar(255)  | ФИО отца                           |
| father_phone        | nvarchar(50)   | Телефон отца                       |
| father_address      | ntext          | Адрес отца                         |
| mother_fio          | nvarchar(255)  | ФИО матери                         |
| mother_phone        | nvarchar(50)   | Телефон матери                     |
| mother_address      | ntext          | Адрес матери                       |

## 👨‍🏫 Преподаватели (Teachers)
| Поле         | Тип            | Комментарий              |
|--------------|----------------|--------------------------|
| id           | int PK         | Уникальный ID            |
| surname      | nvarchar(255)  | Фамилия                  |
| name         | nvarchar(255)  | Имя                      |
| lastname     | nvarchar(255)  | Отчество                 |
| birthday     | date           | Дата рождения            |
| email        | nvarchar(255)  | Электронная почта        |
| phone_number | nvarchar(50)   | Телефон                  |
| photo        | varbinary(MAX) | Фото                     |

### Преподаватели — Доп. информация (Teachers_Info)
| Поле            | Тип           | Комментарий              |
|-----------------|---------------|--------------------------|
| teacher_id      | int PK, FK    | Ссылка на `Teachers`     |
| address         | ntext         | Адрес                    |
| passport_number | nvarchar(100) | Паспорт                  |
| high_school     | nvarchar(255) | ВУЗ                      |
| document_number | nvarchar(100) | Диплом/удостоверение     |
| military        | nvarchar(100) | Воинская обязанность     |
| degree          | nvarchar(100) | Учёная степень           |
| title           | nvarchar(100) | Учёное звание            |
| position        | nvarchar(100) | Должность                |

## 🏫 Специальности (Specialities)
| Поле | Тип           | Комментарий              |
|------|---------------|--------------------------|
| id   | int PK        | Уникальный идентификатор |
| name | nvarchar(255) | Название специальности   |

## 👥 Группы (Groups)
| Поле    | Тип           | Комментарий                     |
|---------|---------------|---------------------------------|
| id      | int PK        | Уникальный ID                   |
| name    | nvarchar(255) | Название группы                 |
| spec_id | int FK        | Ссылка на `Specialities`        |
| course  | int           | Курс обучения                   |

## 📘 Предметы (Subjects)
| Поле | Тип           | Комментарий                |
|------|---------------|----------------------------|
| id   | int PK        | Уникальный ID              |
| name | nvarchar(255) | Название предмета          |

## 📚 Дисциплины (Disciplines)
| Поле          | Тип           | Комментарий                      |
|---------------|---------------|----------------------------------|
| id            | int PK        |                                  |
| subject_id    | int FK        | Ссылка на `Subjects`             |
| semester      | int           |                                  |
| speciality_id | int FK        | Ссылка на `Specialities`         |
| course        | int           | Курс обучения                    |
| lecture       | int           | Часы лекций                      |
| practice      | int           | Часы практики                    |
| lab           | int           | Часы лабораторных                |
| seminar       | int           | Часы семинаров                   |
| control       | nvarchar(100) | Вид контроля                     |

## 🗂 Учебные планы (Curriculums)
| Поле           | Тип           | Комментарий                            |
|----------------|---------------|----------------------------------------|
| id             | int PK        | ID                                     |
| speciality_id  | int FK        | Ссылка на `Specialities`               |
| year           | int           | Год начала действия плана              |
| profile        | nvarchar(255) | Профиль подготовки                     |
| education_form | nvarchar(100) | Форма обучения                         |
| degree         | nvarchar(100) | Степень                                |
| duration       | int           | Продолжительность в годах              |
| approved_date  | date          | Дата утверждения                       |

### Дисциплины в учебном плане (Curriculum_Disciplines)
| Поле          | Тип           | Комментарий                            |
|---------------|---------------|----------------------------------------|
| id            | int PK        | ID                                     |
| curriculum_id | int FK        | Ссылка на `Curriculums`                |
| discipline_id | int FK        | Ссылка на `Disciplines`                |
| semester      | int           | Семестр                                |
| course        | int           | Курс                                   |
| exam          | bit           | Есть экзамен (0/1)                     |
| credit        | bit           | Есть зачёт (0/1)                       |
| coursework    | bit           | Есть курсовая работа                   |
| control_type  | nvarchar(100) | Вид контроля                           |
| credits       | int           | Зачётные единицы                       |

## 🧮 Нагрузка преподавателей (Teachers_Workload)
| Поле          | Тип           | Комментарий                            |
|---------------|---------------|----------------------------------------|
| id            | int PK        | ID                                     |
| teacher_id    | int FK        | Ссылка на `Teachers`                   |
| discipline_id | int FK        | Ссылка на `Disciplines`                |
| type          | nvarchar(100) | Тип занятия (лекция и т.п.)            |
| hours         | int           | Количество часов                       |
| academic_year | nvarchar(20)  | Учебный год (например, 2024/2025)      |
| control_type  | nvarchar(100) | Вид контроля                           |
| group_id      | int FK        | Ссылка на `Groups`                     |

## 🗓 Расписание занятий (Schedule)
| Поле          | Тип           | Комментарий                            |
|---------------|---------------|----------------------------------------|
| id            | int PK        | ID                                     |
| day           | date          | День занятия                           |
| time          | time          | Время начала                           |
| group_id      | int FK        | Ссылка на `Groups`                     |
| discipline_id | int FK        | Ссылка на `Disciplines`                |
| teacher_id    | int FK        | Ссылка на `Teachers`                   |
| auditorium_id | int FK        | Ссылка на `Auditoriums`                |
| type          | nvarchar(100) | Тип занятия (лекция, практика и т.п.)  |

## 🏢 Аудитории (Auditoriums)
| Поле   | Тип            | Комментарий                   |
|--------|----------------|-------------------------------|
| id     | int PK         | Уникальный ID                 |
| number | nvarchar(100)  | Номер аудитории               |
| type   | nvarchar(100)  | Тип (лекц., лаб., пр. и т.п.) |

## ✅ Посещаемость (Attendance)
| Поле          | Тип           | Комментарий                                |
|---------------|---------------|--------------------------------------------|
| id            | int PK        | ID                                         |
| day           | date          | Дата занятия                               |
| time          | time          | Время занятия                              |
| student_id    | int FK        | Ссылка на `Students`                       |
| discipline_id | int FK        | Ссылка на `Disciplines`                    |
| mark          | nvarchar(100) | Отметка (Присутствует, Отсутствует и т.п.) |

## 📈 Успеваемость (Grades)
| Поле          | Тип           | Комментарий                          |
|---------------|---------------|--------------------------------------|
| id            | int PK        | ID                                   |
| day           | date          | Дата оценки                          |
| time          | time          | Время оценки                         |
| student_id    | int FK        | Ссылка на `Students`                 |
| discipline_id | int FK        | Ссылка на `Disciplines`              |
| teacher_id    | int FK        | Ссылка на `Teachers`                 |
| round         | nvarchar(50)  | Этап/тур (1-й, 2-й, финал и т.д.)    |
| mark          | nvarchar(100) | Оценка (число, зачёт, текст)         |


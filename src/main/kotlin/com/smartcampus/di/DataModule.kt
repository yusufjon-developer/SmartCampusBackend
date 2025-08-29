package com.smartcampus.di

import com.smartcampus.data.dao.*
import com.smartcampus.data.repositories.*
import com.smartcampus.domain.repositories.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val dataModule = module {

    singleOf(::AcademicWeeksDao)
    singleOf(::AttendanceDao)
    singleOf(::AuditoriumsDao)
    singleOf(::AuthDao)
    singleOf(::CurriculumsDao)
    singleOf(::GradesDao)
    singleOf(::GroupsDao)
    singleOf(::ScheduleDao)
    singleOf(::SmartCampusProfileDao)
    singleOf(::SpecialitiesDao)
    singleOf(::StudentsDao)
    singleOf(::SubjectsDao)
    singleOf(::SystemAdminDao)
    singleOf(::TeachersDao)
    singleOf(::WorkloadDao)
    singleOf(::WorkloadExecutionDao)
    singleOf(::DisciplinesDao)
    singleOf(::SubjectsDao)

    singleOf(::AcademicWeeksRepositoryImpl) bind AcademicWeeksRepository::class
    singleOf(::AttendanceRepositoryImpl) bind AttendanceRepository::class
    singleOf(::AuditoriumsRepositoryImpl) bind AuditoriumsRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::CurriculumsRepositoryImpl) bind CurriculumsRepository::class
    singleOf(::GradesRepositoryImpl) bind GradesRepository::class
    singleOf(::ScheduleRepositoryImpl) bind ScheduleRepository::class
    singleOf(::StudentsRepositoryImpl) bind StudentsRepository::class
//    singleOf(::SubjectsRepositoryImpl) bind SubjectsRepository::class
    singleOf(::SystemAdminRepositoryImpl) bind SystemAdminRepository::class
    singleOf(::TeachersRepositoryImpl) bind TeachersRepository::class
    singleOf(::WorkloadRepositoryImpl) bind WorkloadRepository::class
    singleOf(::WorkloadExecutionRepositoryImpl) bind WorkloadExecutionRepository::class

    singleOf(::GroupsRepositoryImpl)
    singleOf(::SpecialitiesRepositoryImpl)
}

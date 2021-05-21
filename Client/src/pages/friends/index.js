import { BanOutline as DeclineRequest, CheckCircleOutline as AcceptRequest, CalendarOutline as Calendar, XCircleOutline as DeleteFriendship, LocationMarkerOutline as Location } from '@graywolfai/react-heroicons'
import { useState } from 'react'
import { Shell, Separator } from '../../components'

import { useUser, useFriends } from '../../hooks'

export default function Profile() {
    const { user, createUser, updateUser } = useUser()
    const { friends, deleteFriend, update } = useFriends(user.email)

    return <Shell>
        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <img
                style={{ height: '36rem' }}
                src={user.picture}
                alt={user.name}
                className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
            />
            <Header user={user} />
            <PendingFriendships user={user} friends={friends} deleteFriend={deleteFriend} update={update}/>
            <AcceptedFriendships user={user} friends={friends} deleteFriend={deleteFriend} />
        </div>
    </Shell>
}

function Header({ user }) {
    return <header className='mt-96 relative flex pb-8 mb-8'>
        <img style={{ aspectRatio: '1/1' }}
            src={user.picture}
            alt={user.name}
            className='absolute w-64 rounded-full shadow-xl z-20' />
        <hgroup className='ml-12 flex-1 mt-28'>
            <h1 className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                      text-right text-white text-6xl font-bold
                                      p-6`}>
                {user.name}
            </h1>
            <Info user={user} />
        </hgroup>
    </header>
}
function Info({ user }) {
    return <div className='flex justify-between'>
        <div className='ml-60 flex'>
            <Calendar className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {
                    user.birthday && <>{user.birthday.day}/{user.birthday.month}/{user.birthday.year}</>
                }
            </span>
        </div>
        <div className='flex ml-60'>
            <Location className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {user.country}
            </span>
        </div>
        <span className={`block text-3xl font-semibold text-black w-full px-8 py-4 text-right`}>
            {user.email}
        </span>
    </div>
}

function PendingFriendships({ user, friends, deleteFriend, update }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Solicitudes de amistad</h2>
        <Separator />
        <div className='inline-grid grid-cols-3'>
            <ObtainFriendsNotAccepted user={user} friends={friends} deleteFriend={deleteFriend} update={update}/>
        </div>
    </>
}

function ObtainFriendsNotAccepted({ user, friends, deleteFriend, update }) {
    const submitCancel = friend => async (event) => {
        await deleteFriend(friend)
    }

    const submitAccept = friend => async (event) => {
        await update(friend)
    }

    let render = <></>

    if (friends != null && friends.content != null) {
        render = friends.content.map((friendship) =>
            friendship.confirmed === false
            &&
            (
                friendship.friend === user.email
                &&
                <div key={friendship.id} className='ml-4 mt-6 h-24 bg-white rounded p-4 flex justify-between shadow-md border-2' style={{ minWidth: '470px' }}>
                    <span className='ml-4 mt-4 font-bold'>{friendship.user}</span>
                    <div className='flex mt-3 mr-3'>
                        <DeclineRequest
                            className='cursor-pointer w-8 h-8 mr-2'
                            onClick={submitCancel(friendship.user)}
                        />
                        <AcceptRequest
                            className='cursor-pointer w-8 h-8'
                            onClick={submitAccept(friendship.id)}
                        />
                    </div>
                </div>
            )
        );
    }

    return render
}

function AcceptedFriendships({ user, friends, deleteFriend }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Amigos</h2>
        <Separator />
        <div className='inline-grid grid-cols-3'>
            <ObtainFriendsAccepted user={user} friends={friends} deleteFriend={deleteFriend} />
        </div>
    </>
}

function ObtainFriendsAccepted({ user, friends, deleteFriend }) {
    const submit = friend => async (event) => {
        await deleteFriend(friend)
    }

    let render = <></>

    if (friends != null && friends.content != null) {
        render = friends.content.map((friendship) =>
            friendship.confirmed === true
            &&
            (
                <div key={friendship.id} className='ml-8 mt-6 h-36 bg-white rounded p-4 flex justify-between shadow-md border-2' style={{ minWidth: '450px' }}>
                    <div className='m-auto'>
                        <img
                            style={{ aspectRatio: '1/1' }}
                            src={friendship.picture}
                            alt={friendship.name}
                            className='w-20 rounded-full shadow-xl' />
                    </div>
                    <div>
                        <span className='ml-32 font-bold'>{friendship.name}</span>
                        <div className='flex mt-6 ml-12'>
                            <div className='flex flex-col text-right text-gray-500 ml-12'>
                                <span>Sois amigos desde</span>
                                <span>el {friendship.since.day}/{friendship.since.month}/{friendship.since.year}</span>
                            </div>
                            {
                                friendship.user === user.email
                                    ?
                                    <DeleteFriendship
                                        className='cursor-pointer w-8 h-8 align-middle m-auto ml-4'
                                        onClick={submit(friendship.friend)}
                                    />
                                    :
                                    <DeleteFriendship
                                        className='cursor-pointer w-8 h-8 align-middle m-auto ml-4'
                                        onClick={submit(friendship.user)}
                                    />
                            }
                        </div>
                    </div>
                </div>
            )
        );
    }

    return render
}